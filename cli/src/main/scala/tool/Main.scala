package tool

import java.nio.file.{Files, Paths}

import scala.collection.JavaConverters._
import scala.meta._
import scala.meta.contrib.{DocToken, ScaladocParser}
import scala.meta.internal.semanticdb.TextDocument
import scala.meta.internal.{semanticdb => s}

import tool.Constructor.Arg
import tool.DefinitionBlock.{
  ClassDefinitionBlock,
  ObjectDefinitionBlock,
  TraitDefinitionBlock
}
import tool.ModifierElement.AccessibilityModifierElement
import tool.PackageInfo.PackageElement
import tool.Reference.{Inheritance, Property}

object Main {

  private def extractScaladoc(doc: TextDocument): Seq[ScaladocBlock] = {
    val tokens =
      doc.text.tokenize.getOrElse(throw new Exception("failed to tokenize"))
    val comments = tokens.collect { case c: Token.Comment => c }
    comments.flatMap { comment =>
      ScaladocParser
        .parseScaladoc(comment)
        .getOrElse(throw new Exception("failed to parse scaladoc"))
        .collect {
          case DocToken(_, _, Some(body)) =>
            ScaladocBlock(
              fileName = FileName(doc.uri),
              startLine = comment.pos.startLine,
              endLine = comment.pos.endLine,
              content = body
            )
        }
    }
  }

  def extractDefinitions(
      doc: TextDocument
  ): Seq[DefinitionBlock] = {
    import InitsOps._
    import ModsOps._
    val packages = doc.text.parse[Source].get.collect { case p: Pkg => p }
    packages.flatMap { p =>
      val packageInfo = PackageInfo(p.ref.toString)
      val fileName = FileName(doc.uri)
      p.stats.collect {
        case c: Defn.Class =>
          val ctorArgs = c.ctor.paramss.flatten.map { s =>
            import Constructor._
            Arg(
              ArgName(s.name.toString),
              Constructor.TypeName(s.decltpe.map(_.toString).getOrElse("-"))
            )
          }
          val constructor = Constructor(ctorArgs)
          ClassDefinitionBlock(
            name = DefinitionName(c.name.toString),
            modifier = c.mods.toModifier,
            parents = c.templ.inits.toParents,
            packageInfo = packageInfo,
            constructor = constructor,
            fileName = fileName,
            startLine = c.pos.startLine,
            endLine = c.pos.endLine
          )
        case t: Defn.Trait =>
          TraitDefinitionBlock(
            name = DefinitionName(t.name.toString),
            modifier = t.mods.toModifier,
            parents = t.templ.inits.toParents,
            packageInfo = packageInfo,
            fileName = fileName,
            startLine = t.pos.startLine,
            endLine = t.pos.endLine
          )
        case o: Defn.Object =>
          ObjectDefinitionBlock(
            name = DefinitionName(o.name.toString),
            modifier = o.mods.toModifier,
            parents = o.templ.inits.toParents,
            packageInfo = packageInfo,
            fileName = fileName,
            startLine = o.pos.startLine,
            endLine = o.pos.endLine
          )
      }
    }
  }

  def main(args: Array[String]): Unit = {
    val targetPackageNames = Seq("example.domain")
    args.toList match {
      case path :: Nil =>
        val semanticdbRoot =
          Paths.get(path).resolve("META-INF").resolve("semanticdb")
        val semanticdbFiles = Files
          .walk(semanticdbRoot)
          .iterator()
          .asScala
          .filter(_.getFileName.toString.endsWith(".semanticdb"))
          .toList

        val textDocs = semanticdbFiles.flatMap { file =>
          s.TextDocuments.parseFrom(Files.readAllBytes(file)).documents
        }

        val scaladocs = Scaladocs(textDocs.flatMap(extractScaladoc))
        val definitionBlocks = DefinitionBlocks(
          textDocs.flatMap(extractDefinitions)
        )
        val documentedDefinitions =
          definitionBlocks
            .filterPackages(targetPackageNames)
            .map { definition =>
              val references = definition.resolveReferences(definitionBlocks)
              DocumentIntermediate(
                definition = definition,
                scaladoc = scaladocs.findDocForDefinition(definition),
                references = references
              )
            }

        documentedDefinitions.foreach(println)
      case els =>
        sys.error(s"Expected <path>, obtained $els")
    }
  }
}

/**
  * 1ファイル分のscaladocの集合
  */
case class Scaladocs(blocks: Seq[ScaladocBlock]) {

  /**
    * 渡された定義部に紐づくScaladocを取得する
    */
  def findDocForDefinition(definition: DefinitionBlock): Option[ScaladocBlock] =
    blocks.find(_.endLine == definition.startLine - 1) // 定義部の開始行の直前で終わるScaladocを、定義部に対するScaladocとみなす
}

/**
  * Scaladocのブロック
  */
case class ScaladocBlock(
    fileName: FileName,
    startLine: Int,
    endLine: Int,
    content: String
)

/**
  * classやtraitのブロック
  */
case class DefinitionBlocks(elms: Seq[DefinitionBlock]) {

  /**
    * 指定されたパッケージに含まれるブロックのみに絞り込む
    */
  def filterPackages(targetPackages: Seq[String]): Seq[DefinitionBlock] =
    elms.filter(_.packageInfo.isInAnyPackage(targetPackages))
}

sealed trait DefinitionBlock {
  val name: DefinitionName
  val modifier: Modifier
  val parents: Parents
  val packageInfo: PackageInfo
  val fileName: FileName
  val startLine: Int
  val endLine: Int

  /**
    * 継承やコンストラクタにおける他のクラスやトレイトへの参照を、渡された定義ブロックの集合から解決する
    * @todo 同一名の定義ブロックがあると参照を間違って解決してしまう。本当はimportを見た上でDefintionBlockを絞り込まないといけない。
    * @note 解決できないパターンに対応する必要がある？
    */
  def resolveReferences(from: DefinitionBlocks): References = {
    val inheritances = parents.elms
      .map(_.typeName.value)
      .map { typeName =>
        from.elms.find(_.name.value == typeName)
      }
      .collect { case Some(x) => Inheritance(x) }
    References(inheritances)
  }
}

object DefinitionBlock {

  case class ClassDefinitionBlock(
      name: DefinitionName,
      modifier: Modifier,
      parents: Parents,
      packageInfo: PackageInfo,
      constructor: Constructor,
      fileName: FileName,
      startLine: Int,
      endLine: Int
  ) extends DefinitionBlock {

    override def resolveReferences(from: DefinitionBlocks): References = {
      val inheritances = parents.elms
        .map(_.typeName.value)
        .map { typeName =>
          from.elms.find(_.name.value == typeName)
        }
        .collect { case Some(x) => Inheritance(x) }
      val properties = constructor.args
        .map(_.typeName.value)
        .map { typeName =>
          from.elms.find(_.name.value == typeName)
        }
        .collect { case Some(x) => Property(x) }
      References(inheritances ++ properties)
    }

    def isCaseClass: Boolean =
      modifier.elems.contains(ModifierElement.Case)
  }

  case class TraitDefinitionBlock(
      name: DefinitionName,
      modifier: Modifier,
      parents: Parents,
      packageInfo: PackageInfo,
      fileName: FileName,
      startLine: Int,
      endLine: Int
  ) extends DefinitionBlock

  case class ObjectDefinitionBlock(
      name: DefinitionName,
      modifier: Modifier,
      parents: Parents,
      packageInfo: PackageInfo,
      fileName: FileName,
      startLine: Int,
      endLine: Int
  ) extends DefinitionBlock
}

/**
  * パッケージ
  */
case class PackageInfo(elems: Seq[PackageElement]) {

  /**
    * 指定されたパッケージのいずれかに含まれるならtrue
    */
  def isInAnyPackage(packageNames: Seq[String]): Boolean =
    packageNames.exists(p => toString.startsWith(p))

  override def toString: String = elems.map(_.value).mkString(".")
}

object PackageInfo extends (String => PackageInfo) {
  case class PackageElement(value: String) extends AnyVal

  def apply(packageStr: String): PackageInfo = {
    val elms = packageStr.split('.').map(PackageInfo.PackageElement)
    PackageInfo(elms)
  }
}

/**
  * クラスやトレイトの修飾子
  */
case class Modifier(elems: Seq[ModifierElement]) {

  /**
    * 可視性に関する修飾子を取得する
    */
  def accessibility: String =
    elems
      .find(_.isInstanceOf[AccessibilityModifierElement])
      .map(_.toString)
      .getOrElse("Public")
}
sealed trait ModifierElement

object ModifierElement {
  case object Sealed extends ModifierElement
  case object Final extends ModifierElement
  case object Case extends ModifierElement

  sealed trait AccessibilityModifierElement extends ModifierElement
  case object Private extends AccessibilityModifierElement
  case object Protected extends AccessibilityModifierElement

  case class PackagePrivate(packageName: String)
      extends AccessibilityModifierElement {

    override def toString: String = s"Private[${packageName}]"
  }

  case class PackageProtected(packageName: String)
      extends AccessibilityModifierElement {
    override def toString: String = s"Protected[${packageName}]"
  }
}

/**
  * クラスやトレイトなどの名称
  */
case class DefinitionName(value: String) extends AnyVal

/**
  * クラスのコンストラクタ
  */
case class Constructor(args: Seq[Arg])

object Constructor {

  case class Arg(name: ArgName, typeName: TypeName) {
    override def toString: String = s"${name.value}: ${typeName.value}"
  }
  case class ArgName(value: String) extends AnyVal
  case class TypeName(value: String) extends AnyVal
}

/**
  * ソースコードのファイル名
  */
case class FileName(value: String) extends AnyVal

/**
  * クラスやトレイトが参照する定義
  * @example `case class(a: A) extends B` の場合、 `Seq(Property(A), Inheritance(B))`
  */
case class References(elms: Seq[Reference]) {

  override def toString: String = {
    val content = if (elms.nonEmpty) {
      val items = elms.map {
        case Inheritance(definition) =>
          s"[inherit] ${definition.packageInfo}.${definition.name.value}"
        case Property(definition) =>
          s"[prop] ${definition.packageInfo}.${definition.name.value}"
      }
      items.mkString("\n  ", "\n  ", "")
    } else {
      "-"
    }
    s"references: $content"
  }
}

sealed trait Reference {
  def definition: DefinitionBlock
}

object Reference {
  case class Inheritance(definition: DefinitionBlock) extends Reference
  case class Property(definition: DefinitionBlock) extends Reference
}

/**
  * 生成されるドキュメントの大本となるデータ構造。クラスやトレイトの定義ブロックやScaladoc, 参照などを含む。
  * @param definition 定義ブロック
  * @param references 定義ブロック内に存在する、他の定義ブロックへの参照
  * @param scaladoc 定義ブロックに対応するScaladoc
  */
case class DocumentIntermediate(
    definition: DefinitionBlock,
    references: References,
    scaladoc: Option[ScaladocBlock]
) {

  override def toString: String = {
    val defType = definition match {
      case c: ClassDefinitionBlock =>
        if (c.isCaseClass) "case class" else "class"
      case _: TraitDefinitionBlock  => "trait"
      case _: ObjectDefinitionBlock => "object"
    }

    val base =
      s"""[$defType] ${definition.name.value} ===========================
       |package: ${definition.packageInfo}
       |accessibility: ${definition.modifier.accessibility}
       |parents: ${definition.parents}
       |fileName: ${definition.fileName.value}""".stripMargin

    val constructorInfo = definition match {
      case c: ClassDefinitionBlock =>
        s"constructor:\n  ${c.constructor.args.mkString("\n  ")}"
      case _ => ""
    }

    val scaladocInfo = scaladoc match {
      case Some(doc) => s"description: ${doc.content.replace("\n", " ")}"
      case None      => ""
    }

    s"""$base
      |$constructorInfo
      |$references
      |$scaladocInfo
      |""".stripMargin
  }
}

/**
  * クラスやトレイトの親
  */
case class Parents(elms: Seq[Parent]) {
  override def toString: String = if (elms.isEmpty) "-" else elms.mkString(", ")
}

case class Parent(typeName: Parent.TypeName) {
  override def toString: String = typeName.value
}

object Parent {
  case class TypeName(value: String) extends AnyVal
}

object ModsOps {

  implicit class RichMods(mods: Seq[Mod]) {

    def toModifier: Modifier = {
      val modElms = mods.map(toModElm).collect { case Some(mod) => mod }
      Modifier(modElms)
    }
  }

  private def toModElm(mod: Mod): Option[ModifierElement] = mod match {
    case _: Mod.Case                     => Some(ModifierElement.Case)
    case _: Mod.Sealed                   => Some(ModifierElement.Sealed)
    case _: Mod.Final                    => Some(ModifierElement.Final)
    case Mod.Private(Name.Anonymous())   => Some(ModifierElement.Private)
    case Mod.Protected(Name.Anonymous()) => Some(ModifierElement.Protected)
    case Mod.Private(ref) =>
      Some(ModifierElement.PackagePrivate(ref.toString))
    case Mod.Protected(ref) =>
      Some(ModifierElement.PackageProtected(ref.toString))
    case _ => None
  }
}

object InitsOps {

  implicit class RichInits(inits: Seq[Init]) {

    def toParents: Parents = {
      val elms = inits.map(i => Parent(Parent.TypeName(i.tpe.toString)))
      Parents(elms)
    }
  }
}

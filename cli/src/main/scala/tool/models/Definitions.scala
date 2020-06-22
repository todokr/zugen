package tool.models

import tool.Config.TargetPackageName
import tool.models.Definitions._
import tool.models.DocumentMaterial.DocumentMaterialElement
import tool.models.Modifiers.ModifierElement
import tool.models.References.Reference.{Inheritance, Property}

/**
  * classやtraitの定義ブロック
  */
case class Definitions(blocks: Seq[DefinitionBlock]) {

  /**
    * 指定されたパッケージに含まれるブロックのみに絞り込む
    */
  def filterPackages(targetPackages: Seq[TargetPackageName]): Definitions =
    Definitions(blocks.filter(_.pkg.isInAnyPackage(targetPackages.map(_.value))))

  /**
    * Scaladocとマージして、ドキュメントの生成元データを組み立てる
    */
  def mergeWithScaladoc(scaladocs: Scaladocs): DocumentMaterial = {
    val intermediateElms =
      blocks
        .map { definition =>
          val references = definition.resolveReferences(this)
          DocumentMaterialElement(
            definition = definition,
            scaladoc = scaladocs.findDocForDefinition(definition),
            references = references
          )
        }

    DocumentMaterial(intermediateElms)
  }
}

object Definitions {

  sealed trait DefinitionBlock {
    val name: DefinitionName
    val modifier: Modifiers
    val parents: Parents
    val pkg: Package
    val fileName: FileName
    val startLine: Int
    val endLine: Int

    /**
      * 継承やコンストラクタにおける他のクラスやトレイトへの参照を、渡された定義ブロックの集合から解決する
      * @todo 同一名の定義ブロックがあると参照を間違って解決してしまう。本当はimportを見た上でDefintionBlockを絞り込まないといけない。
      * @todo 参照を解決できないパターンに対応する必要がある？
      */
    def resolveReferences(from: Definitions): References = {
      val inheritances = parents.elms
        .map(_.typeName.value)
        .map { typeName =>
          from.blocks.find(_.name.value == typeName)
        }
        .collect { case Some(x) => Inheritance(x) }
      References(inheritances)
    }
  }

  object DefinitionBlock {

    case class ClassDefinitionBlock(
        name: DefinitionName,
        modifier: Modifiers,
        parents: Parents,
        pkg: Package,
        constructor: Constructor,
        fileName: FileName,
        startLine: Int,
        endLine: Int)
        extends DefinitionBlock {

      override def resolveReferences(from: Definitions): References = {
        val inheritances = parents.elms
          .map(_.typeName.value)
          .map { typeName =>
            from.blocks.find(_.name.value == typeName)
          }
          .collect { case Some(x) => Inheritance(x) }
        val properties = constructor.args
          .map(_.typeName.value)
          .map { typeName =>
            from.blocks.find(_.name.value == typeName)
          }
          .collect { case Some(x) => Property(x) }
        References(inheritances ++ properties)
      }

      def isCaseClass: Boolean =
        modifier.elems.contains(ModifierElement.Case)
    }

    case class TraitDefinitionBlock(
        name: DefinitionName,
        modifier: Modifiers,
        parents: Parents,
        pkg: Package,
        fileName: FileName,
        startLine: Int,
        endLine: Int)
        extends DefinitionBlock

    case class ObjectDefinitionBlock(
        name: DefinitionName,
        modifier: Modifiers,
        parents: Parents,
        pkg: Package,
        fileName: FileName,
        startLine: Int,
        endLine: Int)
        extends DefinitionBlock
  }

}

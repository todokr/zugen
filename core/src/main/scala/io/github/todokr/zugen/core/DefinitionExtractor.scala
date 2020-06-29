package io.github.todokr.zugen.core

import io.github.todokr.zugen.core.models._
import io.github.todokr.zugen.core.models.Constructor._
import io.github.todokr.zugen.core.models.Parents.Parent
import io.github.todokr.zugen.core.models.Package.PackageElement
import io.github.todokr.zugen.core.models.Definitions.DefinitionBlock._
import io.github.todokr.zugen.core.models.Modifiers.{AccessibilityModifierElement, ModifierElement}
import scala.meta._
import scala.meta.internal.semanticdb.SymbolOccurrence.Role
import scala.meta.internal.semanticdb.{SymbolOccurrence, TextDocument}
import scala.util.chaining._

object DefinitionExtractor {
  import Ops._

  /**
    * コードからクラスやトレイトなどの定義ブロックを抜き出す
    */
  def extractDefinitions(docs: Seq[TextDocument]): Definitions =
    docs.flatMap { doc =>
      val packages = doc.text.parse[Source].get.collect { case p: Pkg => p }
      val fileName = FileName(doc.uri)
      val referredFQCNs = doc.occurrences
        .collect {
          case s @ SymbolOccurrence(Some(range), _, _) if s.isClassOrTraitReference =>
            ReferredFQCN(
              startLine = range.startLine,
              endLine = range.endLine,
              fqcn = s.fqcn
            )
        }

      packages.flatMap { p =>
        val pkg = Package(p.ref.toString)
        p.stats.collect {
          case c: Defn.Class =>
            val constructor = resolveConstructor(c.ctor.paramss.flatten, referredFQCNs)
            val parents = resolveParents(c.templ.inits, referredFQCNs)

            ClassDefinitionBlock(
              name = DefinitionName(c.name.toString),
              modifier = c.mods.map(toModElm).collect { case Some(mod) => mod }.pipe(Modifiers(_)),
              parents = parents,
              pkg = pkg,
              constructor = constructor,
              fileName = fileName,
              startLine = c.pos.startLine,
              endLine = c.pos.endLine
            )
          case t: Defn.Trait =>
            val parents = resolveParents(t.templ.inits, referredFQCNs)

            TraitDefinitionBlock(
              name = DefinitionName(t.name.toString),
              modifier = t.mods.map(toModElm).collect { case Some(mod) => mod }.pipe(Modifiers(_)),
              parents = parents,
              pkg = pkg,
              fileName = fileName,
              startLine = t.pos.startLine,
              endLine = t.pos.endLine
            )
          case o: Defn.Object =>
            val parents = resolveParents(o.templ.inits, referredFQCNs)
            ObjectDefinitionBlock(
              name = DefinitionName(o.name.toString),
              modifier = o.mods.map(toModElm).collect { case Some(mod) => mod }.pipe(Modifiers(_)),
              parents = parents,
              pkg = pkg,
              fileName = fileName,
              startLine = o.pos.startLine,
              endLine = o.pos.endLine
            )
        }
      }
    }.pipe(Definitions(_))

  private def resolveParents(inits: Seq[Init], referredFQCNs: Seq[ReferredFQCN]): Parents =
    inits.map { init =>
      val (typeName, typeArgs) = init.tpe match {
        case ap: Type.Apply => (ap.tpe.toString, ap.args.map(_.toString))
        case x              => (x.toString, Seq.empty)
      }
      val resolvedFQCN = referredFQCNs
        .find { r =>
          r.startLine == init.pos.startLine &&
          r.endLine == init.pos.endLine &&
          r.typeName == typeName
        }
        .getOrElse(throw new Exception(s"FQCN for parent not found: typeName = ${typeName}," +
          s" start = ${init.pos.startLine}, end = ${init.pos.endLine}"))
      val tpe = Parent.Tpe(typeName, typeArgs, resolvedFQCN.pkg)
      Parent(tpe)
    }.pipe(Parents(_))

  private def resolveConstructor(
    params: Seq[Term.Param],
    referredFQCNs: Seq[ReferredFQCN]): Constructor =
    params.map { param =>
      val tpe = param.decltpe.getOrElse(throw new Exception(s"Declaration type not found for $param"))
      val (typeName, typeArgs) = tpe match {
        case ap: Type.Apply => (ap.tpe.toString, ap.args.map(_.toString))
        case x              => (x.toString, Seq.empty)
      }
      val resolvedFQCN = referredFQCNs
        .find(_.matches(param.pos.startLine, param.pos.endLine, typeName))
        .getOrElse(throw new Exception(
          s"FQCN for constructor not found: " +
            s"typeName = ${typeName}, typeArgs = ${typeArgs.mkString(",")}, " +
            s"start = ${param.pos.startLine}, end = ${param.pos.endLine}"))
      Arg(
        ArgName(param.name.toString),
        Constructor.Tpe(typeName, typeArgs, resolvedFQCN.pkg)
      )
    }.pipe(Constructor(_))

  private def toModElm(mod: Mod): Option[ModifierElement] =
    mod match {
      case _: Mod.Case                     => Some(ModifierElement.Case)
      case _: Mod.Sealed                   => Some(ModifierElement.Sealed)
      case _: Mod.Final                    => Some(ModifierElement.Final)
      case Mod.Private(Name.Anonymous())   => Some(AccessibilityModifierElement.Private)
      case Mod.Protected(Name.Anonymous()) => Some(AccessibilityModifierElement.Protected)
      case Mod.Private(ref)                => Some(AccessibilityModifierElement.PackagePrivate(ref.toString))
      case Mod.Protected(ref)              => Some(AccessibilityModifierElement.PackageProtected(ref.toString))
      case _                               => None
    }

  /**
    * SemanticDBから取得した参照先
    */
  case class ReferredFQCN(startLine: Int, endLine: Int, fqcn: String) {

    val typeName: String = fqcn.split("/").last
    val pkg: Package = Package(fqcn.split("/").toIndexedSeq.init.map(PackageElement))

    def matches(startLine: Int, endLine: Int, typeName: String): Boolean = {
      this.startLine == startLine && this.endLine == endLine &&
      (this.typeName == typeName || this.typeName == s"Predef.$typeName") // TODO hack
    }
  }

  /**
    * SemanticDBから取得した親クラス・トレイト
    */
  case class ExtractedParent(
    typeName: String,
    typeArgs: Seq[String],
    startLine: Int,
    endLine: Int
  )

  /**
    * SemainticDBから取得したコンストラクタ引数
    */
  case class ExtractedConstructorArg(
    name: String,
    typeName: String,
    typeArgs: Seq[String],
    startLine: Int,
    endLine: Int
  )

  private object Ops {

    implicit class RichSymbolOccurrence(occurrence: SymbolOccurrence) {

      /**
        * クラス、あるいはトレイトの参照の場合true
        */
      def isClassOrTraitReference: Boolean =
        occurrence.role == Role.REFERENCE &&
          occurrence.symbol.endsWith("#") // See: https://scalameta.org/docs/semanticdb/specification.html#symbol-1

      /**
        * SymbolからDescriptorを取り除いたFQCN
        */
      def fqcn: String = occurrence.symbol.init
    }
  }
}

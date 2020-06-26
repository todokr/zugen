package tool

import scala.meta._
import scala.meta.internal.semanticdb.SymbolOccurrence.Role
import scala.meta.internal.semanticdb.{SymbolOccurrence, TextDocument}

import tool.models.Constructor.{Arg, ArgName}
import tool.models.Definitions.DefinitionBlock.{ClassDefinitionBlock, ObjectDefinitionBlock, TraitDefinitionBlock}
import tool.models.Package.PackageElement
import tool.models.Parents.Parent
import tool.models._

object DefinitionExtractor {
  import Ops._

  /**
    * コードからクラスやトレイトなどの定義ブロックを抜き出す。
    */
  def extractDefinitions(docs: Seq[TextDocument]): Definitions = {

    val blocks = docs.flatMap { doc =>
      val packages = doc.text.parse[Source].get.collect { case p: Pkg => p }
      val fileName = FileName(doc.uri)

      // コード中のシンボルが参照しているクラスやトレイトを解決するため、
      // 参照先のFully Qualified Class Nameと出現位置を取得する
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
              modifier = c.mods.toModifier,
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
              modifier = t.mods.toModifier,
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
              modifier = o.mods.toModifier,
              parents = parents,
              pkg = pkg,
              fileName = fileName,
              startLine = o.pos.startLine,
              endLine = o.pos.endLine
            )
        }
      }
    }

    Definitions(blocks)
  }

  private def resolveParents(inits: Seq[Init], referredFQCNs: Seq[ReferredFQCN]): Parents = {
    val parents = inits.map { i =>
      val start = i.pos.startLine
      val end = i.pos.endLine
      val typeName = i.tpe.toString
      val resolvedFQCN = referredFQCNs
        .find(r => r.startLine == start && r.endLine == end && r.typeName == typeName)
        .getOrElse(throw new Exception(s"FQCN for parent not found: $typeName"))
      val tpe = Parent.Tpe(typeName, resolvedFQCN.pkg)
      Parent(tpe)
    }
    Parents(parents)
  }

  private def resolveConstructor(params: Seq[Term.Param], referredFQCNs: Seq[ReferredFQCN]): Constructor = {
    val args = params.map { param =>
      val start = param.pos.startLine
      val end = param.pos.endLine
      val typeName = param.decltpe.map(_.toString).getOrElse(throw new Exception(s"type not found: ${param.name}"))
      val resolvedFQCN = referredFQCNs
        .find(r => r.startLine == start && r.endLine == end && r.typeName == typeName)
        .getOrElse(throw new Exception(s"FQCN for constructor not found: $typeName"))
      Arg(
        ArgName(param.name.toString),
        Constructor.Tpe(typeName, resolvedFQCN.pkg)
      )
    }
    Constructor(args)
  }

  /**
    * SemanticDBから取得した参照先
    */
  case class ReferredFQCN(startLine: Int, endLine: Int, fqcn: String) {

    val typeName: String = fqcn.split("\\.").last
    val pkg: Package = Package(fqcn.split("\\.").init.map(PackageElement))
  }

  private object Ops {
    import Modifiers._

    implicit class RichMods(mods: Seq[Mod]) {

      def toModifier: Modifiers = {
        val modElms = mods.map(toModElm).collect { case Some(mod) => mod }
        Modifiers(modElms)
      }
    }

    private def toModElm(mod: Mod): Option[ModifierElement] = mod match {
      case _: Mod.Case                     => Some(ModifierElement.Case)
      case _: Mod.Sealed                   => Some(ModifierElement.Sealed)
      case _: Mod.Final                    => Some(ModifierElement.Final)
      case Mod.Private(Name.Anonymous())   => Some(ModifierElement.Private)
      case Mod.Protected(Name.Anonymous()) => Some(ModifierElement.Protected)
      case Mod.Private(ref)                => Some(ModifierElement.PackagePrivate(ref.toString))
      case Mod.Protected(ref)              => Some(ModifierElement.PackageProtected(ref.toString))
      case _                               => None
    }

    implicit class RichSymbolOccurrence(occurrence: SymbolOccurrence) {

      /**
        * クラス、あるいはトレイトの参照の場合true
        */
      def isClassOrTraitReference: Boolean =
        occurrence.role == Role.REFERENCE &&
          occurrence.symbol.endsWith("#") // See: https://scalameta.org/docs/semanticdb/specification.html#symbol-1
      /**
        * SymbolからDescriptorを取り除いたFQDN
        */
      def fqcn: String = occurrence.symbol.init
    }
  }
}

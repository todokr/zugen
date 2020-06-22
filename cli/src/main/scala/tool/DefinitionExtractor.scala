package tool

import scala.meta._
import scala.meta.internal.semanticdb.TextDocument

import tool.models._
import tool.models.Definitions.DefinitionBlock.{ClassDefinitionBlock, ObjectDefinitionBlock, TraitDefinitionBlock}

object DefinitionExtractor {
  import Ops._

  /**
    * コードからクラスやトレイトなどの定義ブロックを抜き出す。
    */
  def extractDefinitions(docs: Seq[TextDocument]): Definitions = {
    val blocks = docs.flatMap { doc =>
      val packages = doc.text.parse[Source].get.collect { case p: Pkg => p }
      val fileName = FileName(doc.uri)
      packages.flatMap { p =>
        val pkg = Package(p.ref.toString)
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
              pkg = pkg,
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
              pkg = pkg,
              fileName = fileName,
              startLine = t.pos.startLine,
              endLine = t.pos.endLine
            )
          case o: Defn.Object =>
            ObjectDefinitionBlock(
              name = DefinitionName(o.name.toString),
              modifier = o.mods.toModifier,
              parents = o.templ.inits.toParents,
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

  private object Ops {
    import Modifiers._
    import Parents._

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

    implicit class RichInits(inits: Seq[Init]) {

      def toParents: Parents = Parents(inits.map(i => Parent(Parent.TypeName(i.tpe.toString))))
    }
  }
}

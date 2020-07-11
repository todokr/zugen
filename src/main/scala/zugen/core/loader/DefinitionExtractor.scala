package zugen.core.loader

import scala.meta._
import scala.meta.internal.semanticdb.{SymbolOccurrence, TextDocument}
import scala.util.chaining._

import zugen.core.models.Constructor.{Argument, ArgumentName}
import zugen.core.models.Definitions.DefinitionBlock.{ClassDefinitionBlock, ObjectDefinitionBlock, TraitDefinitionBlock}
import zugen.core.models.Modifiers.AccessibilityModifierElement.{Private, Protected}
import zugen.core.models.Modifiers.{AccessibilityModifierElement, ModifierElement}
import zugen.core.models.Package.PackageElement
import zugen.core.models.Parents.Parent
import zugen.core.models.{Constructor, DefinitionName, Definitions, FileName, Modifiers, Package, Parents}

trait DefinitionExtractor {

  /** extract definition block of class etc. */
  def extractDefinitions(docs: Seq[TextDocument]): Definitions =
    docs
      .flatMap { doc =>
        val source = doc.text.parse[Source].get
        val packages = source.collect { case p: Pkg => p }
        val fileName = FileName(doc.uri)

        val referredFQCNs = doc.occurrences
          .collect {
            case SymbolOccurrence(Some(range), symbol, _) =>
              ReferredFQCN(
                startLine = range.startLine,
                endLine = range.endLine,
                startColumn = range.startCharacter,
                endColumn = range.endCharacter,
                fqcn = symbol.init // TODO hack
              )
          }.sortBy(_.startLine)

        packages.flatMap { p =>
          val pkg = Package(p.ref.toString)
          p.stats.collect {
            case c: Defn.Class =>
              val constructor =
                resolveConstructor(c.ctor.paramss.flatten, referredFQCNs)
              val parents = resolveParents(c.templ.inits, referredFQCNs)

              ClassDefinitionBlock(
                name = DefinitionName(c.name.toString),
                modifier = c.mods
                  .map(toModElm)
                  .collect { case Some(mod) => mod }
                  .pipe(Modifiers(_)),
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
                modifier = t.mods
                  .map(toModElm)
                  .collect { case Some(mod) => mod }
                  .pipe(Modifiers(_)),
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
                modifier = o.mods
                  .map(toModElm)
                  .collect { case Some(mod) => mod }
                  .pipe(Modifiers(_)),
                parents = parents,
                pkg = pkg,
                fileName = fileName,
                startLine = o.pos.startLine,
                endLine = o.pos.endLine
              )
          }
        }
      }
      .pipe(Definitions(_))

  private def resolveParents(inits: Seq[Init], referredFQCNs: Seq[ReferredFQCN]): Parents =
    inits
      .map { init =>
        val (typeName, typeArgs) = init.tpe match {
          // TODO handle more nested case
          case ap: Type.Apply => (ap.tpe.toString, ap.args.map(_.toString)) // ex: ParentClass[TypeParam]
          case x              => (x.toString, Seq.empty)
        }
        val pkg = referredFQCNs
          .find { r =>
            r.startLine == init.pos.startLine &&
            r.endLine == init.pos.endLine &&
            r.startColumn == init.pos.startColumn &&
            r.endColumn == init.pos.endColumn
          } match {
          case Some(fqcn) => fqcn.pkg
          case None =>
            println(
              s"${Console.YELLOW}[WARN]${Console.RESET} FQCN for parent not found: typeName = ${typeName},  ${init.pos.startLine}:${init.pos.endLine} ${init.pos.startColumn}~${init.pos.endColumn}"
            )
            Package.unknown
        }

        val tpe = Parent.ParentType(typeName, typeArgs, pkg)
        Parent(tpe)
      }
      .pipe(Parents(_))

  private def resolveConstructor(
    params: Seq[Term.Param],
    referredFQCNs: Seq[ReferredFQCN]
  ): Constructor =
    params
      .map { param =>
        val tpe = param.decltpe.map {
          // TODO handle more nested case
          case Type.Apply(name, _) => name // ex: Id[T] -> Id
          case x                   => x
        }.getOrElse(
          throw new Exception(s"Declaration type not found for $param")
        )

        val (typeName, typeArgs) = tpe match {
          // TODO handle more nested case
          case ap: Type.Apply => (ap.tpe.toString, ap.args.map(_.toString))
          case x              => (x.toString, Seq.empty)
        }
        val pkg = referredFQCNs
          .find { r =>
            r.startLine == tpe.pos.startLine &&
            r.endLine == tpe.pos.endLine &&
            r.startColumn == tpe.pos.startColumn &&
            r.endColumn == tpe.pos.endColumn
          } match {
          case Some(fqcn) => fqcn.pkg
          case None =>
            println(
              s"${Console.YELLOW}[WARN]${Console.RESET} FQCN for constructor not found: typeName = ${typeName}, typeArgs = ${typeArgs
                .mkString(",")}, ${param.pos.startLine}:${param.pos.endLine} ${param.pos.startColumn}~${param.pos.endColumn}"
            )
            Package.unknown
        }

        Argument(
          ArgumentName(param.name.toString),
          Constructor.ArgumentType(typeName, typeArgs, pkg)
        )
      }
      .pipe(Constructor(_))

  private def toModElm(mod: Mod): Option[ModifierElement] =
    mod match {
      case _: Mod.Case                     => Some(ModifierElement.Case)
      case _: Mod.Sealed                   => Some(ModifierElement.Sealed)
      case _: Mod.Final                    => Some(ModifierElement.Final)
      case Mod.Private(Name.Anonymous())   => Some(Private)
      case Mod.Protected(Name.Anonymous()) => Some(Protected)
      case Mod.Private(ref) =>
        Some(AccessibilityModifierElement.PackagePrivate(ref.toString))
      case Mod.Protected(ref) =>
        Some(AccessibilityModifierElement.PackageProtected(ref.toString))
      case _ => None
    }

  /** Fully Qualified Class Names of symbol occurred in source code */
  case class ReferredFQCN(
    startLine: Int,
    endLine: Int,
    startColumn: Int,
    endColumn: Int,
    fqcn: String
  ) {

    val typeName: String = fqcn.split("/").last
    val pkg: Package = Package(
      fqcn.split("/").toIndexedSeq.init.map(PackageElement)
    )
  }
}

package zugen.core.loader

import scala.meta._
import scala.meta.contrib.{DocToken, ScaladocParser}
import scala.meta.internal.semanticdb.SymbolOccurrence.Role
import scala.meta.internal.semanticdb.{Range, SymbolOccurrence, TextDocument}
import scala.util.chaining._

import zugen.core.loader.ReferredSymbol.InvokedSymbol
import zugen.core.models.Modifier.AccessibilityModifier
import zugen.core.models.TemplateDefinition.{ClassDefinition, ObjectDefinition, TraitDefinition}
import zugen.core.models._

trait SemanticDBTemplateExtractor {

  /** Extract templates from TextDocuments of SemanticDB */
  def extractTemplates(docs: Seq[TextDocument]): TemplateDefinitions =
    docs
      .flatMap { doc =>
        println(s"${doc.uri} -----------------------------------------")
        val source = doc.text.parse[Source].get
        val packages = source.collect { case p: Pkg => p }
        val fileName = FileName(doc.uri)

        val referredFQCNs = doc.occurrences
          .collect {
            case SymbolOccurrence(Some(range), symbol, Role.REFERENCE) => ReferredSymbol.of(symbol, range)
          }

        val scaladocs = {
          val tokens =
            doc.text.tokenize.getOrElse(throw new Exception(s"failed to tokenize code: ${doc.uri}"))
          val fileName = FileName(doc.uri)
          val comments = tokens.collect { case c: Token.Comment => c }
          comments.flatMap { comment =>
            ScaladocParser
              .parseScaladoc(comment)
              .getOrElse(Seq.empty)
              .collect {
                case DocToken(_, _, Some(body)) =>
                  Scaladoc(
                    fileName = fileName,
                    startLine = comment.pos.startLine,
                    endLine = comment.pos.endLine,
                    content = body
                  )
              }
          }
        }.pipe(Scaladocs)

        packages.flatMap { p =>
          val pkg = Package(p.ref.toString)
          p.stats.collect {
            case c: Defn.Class =>
              val definitionName = TemplateDefinitionName(c.name.toString)
              val scaladoc = scaladocs.findByLineNum(c.pos.startLine)
              val constructor =
                resolveConstructor(c.ctor.paramss.flatten, referredFQCNs)

              val parents = resolveParents(c.templ.inits, referredFQCNs)
              resolveInvocations(pkg, definitionName, c.templ, referredFQCNs).foreach(println)

              ClassDefinition(
                name = definitionName,
                modifier = c.mods
                  .map(toModElm)
                  .collect { case Some(mod) => mod }
                  .pipe(Modifiers(_)),
                parents = parents,
                pkg = pkg,
                constructor = constructor,
                methods = Seq.empty,
                fileName = fileName,
                scaladoc = scaladoc,
                startLine = c.pos.startLine,
                endLine = c.pos.endLine
              )
            case t: Defn.Trait =>
              val definitionName = TemplateDefinitionName(t.name.toString)
              val scaladoc = scaladocs.findByLineNum(t.pos.startLine)
              val parents = resolveParents(t.templ.inits, referredFQCNs)

              resolveInvocations(pkg, definitionName, t.templ, referredFQCNs).foreach(println)

              TraitDefinition(
                name = definitionName,
                modifier = t.mods
                  .map(toModElm)
                  .collect { case Some(mod) => mod }
                  .pipe(Modifiers(_)),
                parents = parents,
                pkg = pkg,
                methods = Seq.empty,
                fileName = fileName,
                scaladoc = scaladoc,
                startLine = t.pos.startLine,
                endLine = t.pos.endLine
              )
            case o: Defn.Object =>
              val definitionName = TemplateDefinitionName(o.name.toString)
              val scaladoc = scaladocs.findByLineNum(o.pos.startLine)
              val parents = resolveParents(o.templ.inits, referredFQCNs)

              resolveInvocations(pkg, definitionName, o.templ, referredFQCNs).foreach(println)

              ObjectDefinition(
                name = TemplateDefinitionName(o.name.toString),
                modifier = o.mods
                  .map(toModElm)
                  .collect { case Some(mod) => mod }
                  .pipe(Modifiers(_)),
                parents = parents,
                pkg = pkg,
                methods = Seq.empty,
                fileName = fileName,
                scaladoc = scaladoc,
                startLine = o.pos.startLine,
                endLine = o.pos.endLine
              )
          }
        }
      }.pipe(TemplateDefinitions(_))

  private def resolveParents(inits: Seq[Init], referredFQCNs: Seq[ReferredSymbol]): Parents =
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

        val tpe = ParentType(typeName, typeArgs, pkg)
        Parent(tpe)
      }
      .pipe(Parents)

  private def resolveConstructor(
    params: Seq[Term.Param],
    referredFQCNs: Seq[ReferredSymbol]
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

        ConstructorArgument(
          ConstructorArgumentName(param.name.toString),
          ConstructorArgumentType(typeName, typeArgs, pkg)
        )
      }
      .pipe(Constructor)

  def resolveInvocations(
    pkg: Package,
    templateDefinitionName: TemplateDefinitionName,
    template: Template,
    referredSymbols: Seq[ReferredSymbol]): Seq[Method] = {
    val methods = template.stats.collect { case d: Defn.Def => d }
    methods.map { method =>
      val invokes = method.body.collect {
        case a: Term.Apply =>
          val (targetName, pos) = a.fun match {
            case s: Term.Select  => s.name.value -> s.name.pos
            case name: Term.Name => name.value -> name.pos
            case x               => x.toString -> x.pos
          }

          referredSymbols.collectFirst {
            case symbol: InvokedSymbol
                if symbol.startLine == pos.startLine &&
                  symbol.endLine == pos.endLine &&
                  symbol.startColumn == pos.startColumn &&
                  symbol.endColumn == pos.endColumn =>
              Invoke(symbol.pkg, symbol.templateDefinitionName, MethodName(targetName))
          }
      }.collect { case Some(x) => x }
      Method(pkg, templateDefinitionName, MethodName(method.name.toString), invokes)
    }
  }

  private def toModElm(mod: Mod): Option[Modifier] =
    mod match {
      case _: Mod.Case                     => Some(Modifier.Case)
      case _: Mod.Sealed                   => Some(Modifier.Sealed)
      case _: Mod.Final                    => Some(Modifier.Final)
      case Mod.Private(Name.Anonymous())   => Some(AccessibilityModifier.Private)
      case Mod.Protected(Name.Anonymous()) => Some(AccessibilityModifier.Protected)
      case Mod.Private(ref)                => Some(AccessibilityModifier.PackagePrivate(ref.toString))
      case Mod.Protected(ref)              => Some(AccessibilityModifier.PackageProtected(ref.toString))
      case _                               => None
    }

}

/** A referred symbol occurred in source code */
sealed trait ReferredSymbol {
  val startLine: Int
  val endLine: Int
  val startColumn: Int
  val endColumn: Int
  val symbol: String
  val pkg: Package = Package(symbol.split("/").toIndexedSeq.init.map(QualId))
}
object ReferredSymbol {

  private val MethodInvocationSuffix = "()."

  def of(symbol: String, range: Range): ReferredSymbol =
    symbol match {
      case s if s.endsWith(MethodInvocationSuffix) =>
        InvokedSymbol(
          startLine = range.startLine,
          endLine = range.endLine,
          startColumn = range.startCharacter,
          endColumn = range.endCharacter,
          symbol = symbol
        )
      case _ =>
        PlainSymbol(
          startLine = range.startLine,
          endLine = range.endLine,
          startColumn = range.startCharacter,
          endColumn = range.endCharacter,
          symbol = symbol
        )
    }

  final case class PlainSymbol(
    startLine: Int,
    endLine: Int,
    startColumn: Int,
    endColumn: Int,
    symbol: String
  ) extends ReferredSymbol

  final case class InvokedSymbol(
    startLine: Int,
    endLine: Int,
    startColumn: Int,
    endColumn: Int,
    symbol: String
  ) extends ReferredSymbol {

    val templateDefinitionName: TemplateDefinitionName =
      symbol.split("/").last.split("""[#\\.]""").head.pipe(TemplateDefinitionName)
  }
}

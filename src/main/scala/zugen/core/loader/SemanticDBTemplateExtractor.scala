package zugen.core.loader

import scala.meta._
import scala.meta.contrib.{DocToken, ScaladocParser}
import scala.meta.internal.semanticdb.SymbolOccurrence.Role
import scala.meta.internal.semanticdb.{SymbolOccurrence, TextDocument}
import scala.util.chaining._

import zugen.core.loader.ReferredSymbol.InvokedSymbol
import zugen.core.models.Modifier.AccessibilityModifier
import zugen.core.models.TemplateDefinition.{ClassDefinition, ObjectDefinition, TraitDefinition}
import zugen.core.models._

trait SemanticDBTemplateExtractor {

  /** Extract templates from TextDocuments of SemanticDB */
  def extractTemplateDefinitions(docs: Seq[TextDocument]): TemplateDefinitions =
    docs
      .flatMap { doc =>
        println(s"processing... ${doc.uri}")
        val fileName = FileName(doc.uri)
        val source = doc.text.parse[Source].get
        val pkg = source.collect { case p: Pkg => p.ref.toString }.mkString(".").pipe(Package)
        val referredSymbols = doc.occurrences.collect {
          case SymbolOccurrence(Some(range), symbol, Role.REFERENCE) => ReferredSymbol.of(symbol, range)
        }.pipe(ReferredSymbols)
        val scaladocs = {
          val tokens = doc.text.tokenize.get
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

        source.collect {
          case c: Defn.Class =>
            val definitionName = TemplateDefinitionName(c.name.value)
            val scaladoc = scaladocs.findByLineNum(c.pos.startLine)
            val constructor = resolveConstructor(c.ctor.paramss.flatten, referredSymbols)
            val parents = resolveParents(c.templ.inits, referredSymbols)
            val methods = resolveMethods(pkg, definitionName, c.templ, referredSymbols)

            ClassDefinition(
              name = definitionName,
              modifier = c.mods
                .map(toModElm)
                .collect { case Some(mod) => mod }
                .pipe(Modifiers(_)),
              parents = parents,
              pkg = pkg,
              constructor = constructor,
              methods = methods,
              fileName = fileName,
              scaladoc = scaladoc,
              startLine = c.pos.startLine,
              endLine = c.pos.endLine
            )
          case t: Defn.Trait =>
            val definitionName = TemplateDefinitionName(t.name.value)
            val scaladoc = scaladocs.findByLineNum(t.pos.startLine)
            val parents = resolveParents(t.templ.inits, referredSymbols)
            val methods = resolveMethods(pkg, definitionName, t.templ, referredSymbols)

            TraitDefinition(
              name = definitionName,
              modifier = t.mods
                .map(toModElm)
                .collect { case Some(mod) => mod }
                .pipe(Modifiers(_)),
              parents = parents,
              pkg = pkg,
              methods = methods,
              fileName = fileName,
              scaladoc = scaladoc,
              startLine = t.pos.startLine,
              endLine = t.pos.endLine
            )
          case o: Defn.Object =>
            val definitionName = TemplateDefinitionName(o.name.value)
            val scaladoc = scaladocs.findByLineNum(o.pos.startLine)
            val parents = resolveParents(o.templ.inits, referredSymbols)
            val methods = resolveMethods(pkg, definitionName, o.templ, referredSymbols)

            ObjectDefinition(
              name = TemplateDefinitionName(o.name.toString),
              modifier = o.mods
                .map(toModElm)
                .collect { case Some(mod) => mod }
                .pipe(Modifiers(_)),
              parents = parents,
              pkg = pkg,
              methods = methods,
              fileName = fileName,
              scaladoc = scaladoc,
              startLine = o.pos.startLine,
              endLine = o.pos.endLine
            )
        }

      }.pipe(TemplateDefinitions)

  private def resolveParents(inits: Seq[Init], referredSymbols: ReferredSymbols): Parents =
    inits.map { init =>
      val (typeName, typeArgs) = deconstructType(init.tpe)
      val pkg = referredSymbols
        .findByPosition(init.tpe.pos)
        .map(_.pkg)
        .getOrElse {
          println(
            s"${Console.YELLOW}[WARN]${Console.RESET} FQCN for parent not found: typeName = ${typeName},  ${init.pos.startLine}:${init.pos.endLine} ${init.pos.startColumn}~${init.pos.endColumn}"
          )
          Package.unknown
        }
      ParentType(typeName.value, typeArgs.map(_.value), pkg).pipe(Parent)
    }.pipe(Parents)

  private def resolveConstructor(
    params: Seq[Term.Param],
    referredSymbols: ReferredSymbols
  ): Constructor =
    params
      .map { param =>
        val (typeName, typeArgs) =
          param.decltpe.map(deconstructType)
            .getOrElse(throw new Exception(s"Declaration type not found for $param"))

        val pkg = referredSymbols
          .findByPosition(typeName.pos)
          .map(_.pkg)
          .getOrElse {
            println(
              s"${Console.YELLOW}[WARN]${Console.RESET} FQCN for constructor not found: typeName = ${typeName}, typeArgs = ${typeArgs
                .mkString(",")}, ${param.pos.startLine}:${param.pos.endLine} ${param.pos.startColumn}~${param.pos.endColumn}"
            )
            Package.unknown
          }
        ConstructorArgument(
          ConstructorArgumentName(param.name.toString),
          ConstructorArgumentType(typeName.value, typeArgs.map(_.value), pkg)
        )
      }
      .pipe(Constructor)

  private def resolveMethods(
    pkg: Package,
    templateDefinitionName: TemplateDefinitionName,
    template: Template,
    referredSymbols: ReferredSymbols): Seq[Method] = {
    val methods = template.stats.collect { case d: Defn.Def => d }
    methods.map { method =>
      val invokes = method.body.collect {
        case Term.Apply(fun, _) =>
          val (targetName, pos) = fun match {
            case s: Term.Select  => s.name.value -> s.name.pos
            case name: Term.Name => name.value -> name.pos
            case x               => x.toString -> x.pos
          }
          referredSymbols.findByPosition(pos).collect {
            case s: InvokedSymbol => InvokeTarget(s.pkg, s.templateDefinitionName, MethodName(targetName))
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

  private def deconstructType(tpe: Type): (Type.Name, Seq[Type.Name]) =
    tpe match {
      case n: Type.Name => // just a type name (ex. String)
        n -> Seq.empty[Type.Name]
      case Type.Select(_, tpe) => // plain type (ex. controllers.OrderController)
        tpe.collect { case name: Type.Name => name }.head -> Seq.empty[Type.Name]
      case Type.Apply(tpe, args) => // with type param (ex. Id[T])
        tpe.collect { case name: Type.Name => name }.head -> args.collect { case n: Type.Name => n }
      case Type.ByName(tpe) => // by name type (ex. => String)
        tpe.collect { case name: Type.Name => name }.head -> Seq.empty[Type.Name]
      case x =>
        println(
          s"${Console.YELLOW}[WARN]${Console.RESET} unknown type. class=${x.getClass.getName}, type=${x}")
        x.collect { case name: Type.Name => name }.head -> Seq.empty[Type.Name]
    }
}

/** Referred symbols occurred in source code */
final case class ReferredSymbols(elms: Seq[ReferredSymbol]) {

  def findByPosition(pos: Position): Option[ReferredSymbol] =
    elms.find { s =>
      s.startLine == pos.startLine &&
      s.endLine == pos.endLine &&
      s.startColumn == pos.startColumn &&
      s.endColumn == pos.endColumn
    }
}

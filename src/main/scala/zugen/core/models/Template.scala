package zugen.core.models

import scala.util.chaining._

import zugen.core.models.References.ProjectExternalReference.{ProjectExternalInheritance, ProjectExternalProperty}
import zugen.core.models.References.ProjectInternalReference.{ProjectInternalInheritance, ProjectInternalProperty}

/** Classe, trait and object
  * see: https://scala-lang.org/files/archive/spec/2.13/05-classes-and-objects.html
  */
sealed trait Template {
  val pkg: Package
  val name: TemplateName
  val modifier: Modifiers
  val parents: Parents
  val fileName: FileName
  val startLine: Int
  val endLine: Int

  /** resolve reference to other definition block */
  def resolveReferences(from: Templates): References =
    parents.elms
      .map { parent =>
        val defBlock = from.elms.find(b => b.pkg == parent.tpe.pkg && b.name.value == parent.tpe.typeName)
        defBlock -> parent.tpe
      }
      .collect {
        case (Some(block), _) => ProjectInternalInheritance(block)
        case (None, tpe)      => ProjectExternalInheritance(tpe.pkg, tpe.typeName)
      }.pipe(References(_))

  def isInAnyPackage(targets: Seq[Package]): Boolean = targets.exists(pkg.isInPackage)
}

object Template {

  case class ClassTemplate(
    pkg: Package,
    name: TemplateName,
    modifier: Modifiers,
    parents: Parents,
    constructor: Constructor,
    fileName: FileName,
    startLine: Int,
    endLine: Int
  ) extends Template {

    /** In addition to inheritances, props of constructors have to be resolved for class template */
    override def resolveReferences(from: Templates): References = {
      val inheritances = super.resolveReferences(from)
      val properties = constructor.args
        .map { arg =>
          val defBlock = from.elms.find(b => b.pkg == arg.tpe.pkg && b.name.value == arg.tpe.typeName)
          arg -> defBlock
        }
        .collect {
          case (arg, Some(block)) => ProjectInternalProperty(arg.name.toString, block)
          case (arg, None)        => ProjectExternalProperty(arg.tpe.pkg, arg.tpe.typeName)
        }
      References(inheritances.elms ++ properties)
    }
  }

  case class TraitTemplate(
    pkg: Package,
    name: TemplateName,
    modifier: Modifiers,
    parents: Parents,
    fileName: FileName,
    startLine: Int,
    endLine: Int
  ) extends Template

  case class ObjectTemplate(
    pkg: Package,
    name: TemplateName,
    modifier: Modifiers,
    parents: Parents,
    fileName: FileName,
    startLine: Int,
    endLine: Int
  ) extends Template
}

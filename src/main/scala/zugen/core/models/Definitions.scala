package zugen.core.models

import scala.util.chaining._

import zugen.core.models.Definitions._
import zugen.core.models.DocumentMaterial.DocumentMaterialElement
import zugen.core.models.References.ExternalReference.{ExternalInheritance, ExternalProperty}
import zugen.core.models.References.InternalReference.{InternalInheritance, InternalProperty}

/**
  * definition blocks of class etc.
  */
case class Definitions(blocks: Seq[DefinitionBlock]) {

  def mergeWithScaladoc(scaladocs: Scaladocs): DocumentMaterial =
    blocks
      .map { definition =>
        val references = definition.resolveReferences(this)
        DocumentMaterialElement(
          definition = definition,
          scaladoc = scaladocs.findDocForDefinition(definition),
          references = references
        )
      }.pipe(DocumentMaterial(_))
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
      * resolve reference to other definition block
      */
    def resolveReferences(from: Definitions): References =
      parents.elms
        .map { parent =>
          val defBlock = from.blocks.find(b => b.pkg == parent.tpe.pkg && b.name.value == parent.tpe.typeName)
          defBlock -> parent.tpe
        }
        .collect {
          case (Some(block), _) => InternalInheritance(block)
          case (None, tpe)      => ExternalInheritance(tpe.pkg, tpe.typeName)
        }.pipe(References(_))

    def isInAnyPackage(targets: Seq[Package]): Boolean = targets.exists(pkg.isInPackage)
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
      endLine: Int
    ) extends DefinitionBlock {

      override def resolveReferences(from: Definitions): References = {
        val inheritances = super.resolveReferences(from)
        val properties = constructor.args
          .map { arg =>
            val defBlock = from.blocks.find(b => b.pkg == arg.tpe.pkg && b.name.value == arg.tpe.typeName)
            defBlock -> arg.tpe
          }
          .collect {
            case (Some(block), _) => InternalProperty(block)
            case (None, tpe)      => ExternalProperty(tpe.pkg, tpe.typeName)
          }
        References(inheritances.elms ++ properties)
      }
    }

    case class TraitDefinitionBlock(
      name: DefinitionName,
      modifier: Modifiers,
      parents: Parents,
      pkg: Package,
      fileName: FileName,
      startLine: Int,
      endLine: Int
    ) extends DefinitionBlock

    case class ObjectDefinitionBlock(
      name: DefinitionName,
      modifier: Modifiers,
      parents: Parents,
      pkg: Package,
      fileName: FileName,
      startLine: Int,
      endLine: Int
    ) extends DefinitionBlock
  }
}

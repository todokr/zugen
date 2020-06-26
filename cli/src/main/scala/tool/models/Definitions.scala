package tool.models

import tool.models.Definitions._
import tool.models.DocumentMaterial.DocumentMaterialElement
import tool.models.Modifiers.ModifierElement
import tool.models.References.ExternalReference.ExternalInheritance
import tool.models.References.InternalReference.{InternalInheritance, InternalProperty}

/**
  * classやtraitの定義ブロック
  */
case class Definitions(blocks: Seq[DefinitionBlock]) {

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
      * 継承やコンストラクタにおける他のクラスやトレイトへの参照を解決する。
      * 参照先が定義ブロック内で解決できればドキュメント生成対象パッケージ内部の参照、解決できなければ外部への参照として扱う。
      */
    def resolveReferences(from: Definitions): References = {
      val inheritances = parents.elms
        .map { parent =>
          val defBlock = from.blocks.find(b => b.pkg == parent.tpe.pkg && b.name.value == parent.tpe.typeName)
          defBlock -> parent.tpe
        }
        .collect {
          case (Some(block), _) => InternalInheritance(block)
          case (None, tpe)      => ExternalInheritance(tpe.pkg, tpe.typeName)
        }
      References(inheritances)
    }

    /**
      * 指定されたパッケージのいずれかに含まれている際にtrue
      */
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
        endLine: Int)
        extends DefinitionBlock {

      override def resolveReferences(from: Definitions): References = {
        val inheritances = parents.elms
          .map { parent =>
            val defBlock = from.blocks.find(b => b.pkg == parent.tpe.pkg && b.name.value == parent.tpe.typeName)
            defBlock -> parent.tpe
          }
          .collect {
            case (Some(block), _) => InternalInheritance(block)
            case (None, tpe)      => ExternalInheritance(tpe.pkg, tpe.typeName)
          }
        val properties = constructor.args
          .map { arg =>
            val defBlock = from.blocks.find(b => b.pkg == arg.tpe.pkg && b.name.value == arg.tpe.typeName)
            defBlock -> arg.tpe
          }
          .collect {
            case (Some(block), _) => InternalProperty(block)
            case (None, tpe)      => ExternalInheritance(tpe.pkg, tpe.typeName)
          }
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

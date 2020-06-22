package tool.models

import java.nio.file.Files

import tool.Config
import tool.Config.DocumentType
import tool.models.Definitions.DefinitionBlock
import tool.models.Definitions.DefinitionBlock.{ClassDefinitionBlock, ObjectDefinitionBlock, TraitDefinitionBlock}
import tool.models.DocumentMaterial.DocumentMaterialElement
import tool.models.DocumentWriter.WrittenDocumentPath
import tool.models.Scaladocs.ScaladocBlock

/**
  * 生成されるドキュメントの大本となるデータ構造。クラスやトレイトの定義ブロックやScaladoc, 参照などを含む。
  */
case class DocumentMaterial(elms: Seq[DocumentMaterialElement]) {

  /**
    * ドキュメントや図を書き出す。
    */
  def writeDocument(
      documentType: DocumentType)(implicit writer: DocumentWriter, config: Config): WrittenDocumentPath = {
    if (!config.documentPath.exists) {
      Files.createDirectories(config.documentPath.value)
    }
    writer.write(this, documentType, config.documentPath)
  }
}

object DocumentMaterial {

  /**
    * @param definition 定義ブロック
    * @param references 定義ブロック内に存在する、他の定義ブロックへの参照
    * @param scaladoc 定義ブロックに対応するScaladoc
    */
  case class DocumentMaterialElement(
      definition: DefinitionBlock,
      references: References,
      scaladoc: Option[ScaladocBlock]) {

    override def toString: String = {
      val defType = definition match {
        case c: ClassDefinitionBlock =>
          if (c.isCaseClass) "case class" else "class"
        case _: TraitDefinitionBlock  => "trait"
        case _: ObjectDefinitionBlock => "object"
      }

      val base =
        s"""
           |[$defType] ${definition.name.value} ===========================
           |package: ${definition.pkg}
           |accessibility: ${definition.modifier.accessibility}
           |fileName: ${definition.fileName.value}""".stripMargin

      val parentsInfo =
        if (definition.parents.elms.nonEmpty) s"parents: ${definition.parents.elms.mkString(", ")}"
        else ""

      val constructorInfo = definition match {
        case c: ClassDefinitionBlock =>
          s"constructor:\n  ${c.constructor.args.mkString("\n  ")}"
        case _ => ""
      }

      val referenceInfo = if (references.elms.nonEmpty) s"references:\n  ${references.elms.mkString("\n  ")}" else ""

      val scaladocInfo = scaladoc match {
        case Some(doc) => s"description: ${doc.content.replace("\n", " ")}"
        case None      => ""
      }

      Seq(base, parentsInfo, constructorInfo, referenceInfo, scaladocInfo).filter(_.nonEmpty).mkString("\n")
    }
  }
}

package tool.models

import java.nio.file.Files

import tool.Config
import tool.Config.GenDocumentType
import tool.document.DocumentWriter
import tool.document.DocumentWriter.WrittenDocumentPath
import tool.models.Definitions.DefinitionBlock
import tool.models.DocumentMaterial.DocumentMaterialElement
import tool.models.Scaladocs.ScaladocBlock

/**
  * 生成されるドキュメントの大本となるデータ構造。クラスやトレイトの定義ブロックやScaladoc, 参照などを含む。
  */
case class DocumentMaterial(elms: Seq[DocumentMaterialElement]) {

  /**
    * ドキュメントや図を書き出す
    */
  def writeDocument(
      documentType: GenDocumentType)(implicit writer: DocumentWriter, config: Config): WrittenDocumentPath = {
    if (!config.documentPath.exists) {
      Files.createDirectories(config.documentPath.value)
    }
    writer.write(this, documentType, config)
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
      scaladoc: Option[ScaladocBlock])
}

package tool.models

import java.nio.file.Path

import tool.Config.{DocumentPath, DocumentType}
import tool.models.DocumentWriter.WrittenDocumentPath

trait DocumentWriter {

  def write(
      intermediates: DocumentMaterial,
      documentType: DocumentType,
      documentPath: DocumentPath): WrittenDocumentPath
}

object DocumentWriter {
  case class WrittenDocumentPath(value: Path) extends AnyVal
}

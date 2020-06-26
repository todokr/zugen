package tool.document

import java.nio.file.Path

import tool.Config
import tool.Config.GenDocumentType
import tool.document.DocumentWriter.WrittenDocumentPath
import tool.models.DocumentMaterial

trait DocumentWriter {

  def write(intermediates: DocumentMaterial, documentType: GenDocumentType, config: Config): WrittenDocumentPath
}

object DocumentWriter {
  case class WrittenDocumentPath(value: Path) extends AnyVal
}

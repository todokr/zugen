package tool.models

import java.nio.file.Path

import tool.Config
import tool.Config.GenDocumentType
import tool.models.DocumentWriter.WrittenDocumentPath

trait DocumentWriter {

  def write(intermediates: DocumentMaterial, documentType: GenDocumentType, config: Config): WrittenDocumentPath
}

object DocumentWriter {
  case class WrittenDocumentPath(value: Path) extends AnyVal
}

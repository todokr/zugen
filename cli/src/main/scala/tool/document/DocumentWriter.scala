package tool.document

import java.nio.file.Path

import tool.Config
import tool.document.DocumentWriter.GeneratedDocumentPath
import tool.models.DocumentMaterial

trait DocumentWriter {

  def write(material: DocumentMaterial, config: Config): Seq[GeneratedDocumentPath]
}

object DocumentWriter {
  case class GeneratedDocumentPath(value: Path) extends AnyVal
}

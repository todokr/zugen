package zugen.core.document

import java.nio.file.Path

import zugen.core.Config
import zugen.core.document.DocumentWriter.GeneratedDocumentPath
import zugen.core.models.DocumentMaterial

trait DocumentWriter {

  def write(material: DocumentMaterial, config: Config): Seq[GeneratedDocumentPath]
}

object DocumentWriter {
  case class GeneratedDocumentPath(value: Path) extends AnyVal
}

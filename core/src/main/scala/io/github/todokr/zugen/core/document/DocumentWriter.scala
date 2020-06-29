package io.github.todokr.zugen.core.document

import java.nio.file.Path

import io.github.todokr.zugen.core.Config
import io.github.todokr.zugen.core.document.DocumentWriter.GeneratedDocumentPath
import io.github.todokr.zugen.core.models.DocumentMaterial

trait DocumentWriter {

  def write(material: DocumentMaterial, config: Config): Seq[GeneratedDocumentPath]
}

object DocumentWriter {
  case class GeneratedDocumentPath(value: Path) extends AnyVal
}

package zugen.core.config

import java.nio.file.{Files, Path, Paths}

/** directory path of documents to generate */
final case class DocumentPath(value: Path) extends AnyVal {
  def exists: Boolean = Files.exists(value)
}

object DocumentPath extends (String => DocumentPath) {
  def apply(value: String): DocumentPath = DocumentPath(Paths.get(value))
}

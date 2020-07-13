package zugen.core.config

import java.nio.file.{Path, Paths}

/** classes directory path of target project */
final case class ClassesPath(value: Path) extends AnyVal

object ClassesPath extends (String => ClassesPath) {
  def apply(value: String): ClassesPath = ClassesPath(Paths.get(value))
}

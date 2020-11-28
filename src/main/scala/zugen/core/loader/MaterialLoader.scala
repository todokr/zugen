package zugen.core.loader

import zugen.core.config.ClassesPath
import zugen.core.models.DocumentMaterials

trait MaterialLoader {

  /** load document material from source specified in given paths */
  def load(classesPaths: Seq[ClassesPath]): DocumentMaterials
}

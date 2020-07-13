package zugen.core.loader

import zugen.core.config.Config
import zugen.core.models.DocumentMaterials

trait MaterialLoader {

  /** load document material from source specified in given config */
  def load(config: Config): DocumentMaterials
}

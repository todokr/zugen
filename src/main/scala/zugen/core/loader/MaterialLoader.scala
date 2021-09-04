package zugen.core.loader

import zugen.core.Zugen.ProjectStructure
import zugen.core.models.DocumentMaterials

trait MaterialLoader {

  /** load document material from source specified in given project */
  def load(project: ProjectStructure): DocumentMaterials
}

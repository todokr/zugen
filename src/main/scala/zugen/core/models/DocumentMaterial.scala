package zugen.core.models

import zugen.core.models.Definitions.DefinitionBlock
import zugen.core.models.DocumentMaterial.DocumentMaterialElement
import zugen.core.models.Scaladocs.ScaladocBlock

/** material of zugen document */
case class DocumentMaterial(elms: Seq[DocumentMaterialElement])

object DocumentMaterial {

  case class DocumentMaterialElement(
    definition: DefinitionBlock,
    references: References,
    scaladoc: Option[ScaladocBlock]
  )
}

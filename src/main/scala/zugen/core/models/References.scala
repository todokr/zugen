package zugen.core.models

import zugen.core.models.Definitions.DefinitionBlock
import zugen.core.models.References.Reference

/**
  * references that class etc. refers
  */
case class References(elms: Seq[Reference])

object References {

  sealed trait Reference

  sealed trait InternalReference extends Reference {
    def definition: DefinitionBlock
  }

  object InternalReference {
    final case class InternalInheritance(definition: DefinitionBlock) extends InternalReference
    final case class InternalProperty(definition: DefinitionBlock) extends InternalReference
  }

  sealed trait ExternalReference extends Reference {
    def pkg: Package
    def typeName: String
  }

  object ExternalReference {
    final case class ExternalInheritance(pkg: Package, typeName: String) extends ExternalReference
    final case class ExternalProperty(pkg: Package, typeName: String) extends ExternalReference
  }
}

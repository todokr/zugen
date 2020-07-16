package zugen.core.models

import zugen.core.models.References.Reference

/** references that class etc. refers */
final case class References(elms: Seq[Reference])

object References {

  sealed trait Reference

  sealed trait ProjectInternalReference extends Reference {
    def definition: TemplateDefinition
  }

  object ProjectInternalReference {
    final case class ProjectInternalInheritance(definition: TemplateDefinition) extends ProjectInternalReference
    final case class ProjectInternalProperty(
      memberName: String,
      definition: TemplateDefinition
    ) extends ProjectInternalReference
  }

  sealed trait ProjectExternalReference extends Reference {
    def pkg: Package
    def typeName: String
  }

  object ProjectExternalReference {
    final case class ProjectExternalInheritance(pkg: Package, typeName: String) extends ProjectExternalReference
    final case class ProjectExternalProperty(pkg: Package, typeName: String) extends ProjectExternalReference
  }
}

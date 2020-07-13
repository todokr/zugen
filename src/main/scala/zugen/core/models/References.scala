package zugen.core.models

import zugen.core.models.References.Reference

/** references that class etc. refers */
case class References(elms: Seq[Reference])

object References {

  sealed trait Reference

  sealed trait ProjectInternalReference extends Reference {
    def definition: Template
  }

  object ProjectInternalReference {
    final case class ProjectInternalInheritance(definition: Template) extends ProjectInternalReference
    final case class ProjectInternalProperty(
      memberName: String,
      definition: Template
    ) extends ProjectInternalReference
  }

  sealed trait ProjectExternalReference extends Reference {
    def pkg: Packages
    def typeName: String
  }

  object ProjectExternalReference {
    final case class ProjectExternalInheritance(pkg: Packages, typeName: String) extends ProjectExternalReference
    final case class ProjectExternalProperty(pkg: Packages, typeName: String) extends ProjectExternalReference
  }
}

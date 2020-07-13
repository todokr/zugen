package zugen.core.models

/** A material of zugen document */
case class DocumentMaterial(
  template: Template,
  references: References,
  scaladoc: Option[Scaladoc]
)

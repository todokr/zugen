package zugen.core.models

/** A qualified identifier
  * see: https://scala-lang.org/files/archive/spec/2.13/09-top-level-definitions.html#package-references
  */
final case class QualId(value: String) extends AnyVal {
  override def toString: String = value
}

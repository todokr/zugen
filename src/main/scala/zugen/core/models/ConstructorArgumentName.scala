package zugen.core.models

final case class ConstructorArgumentName(value: String) extends AnyVal {
  override def toString: String = value
}

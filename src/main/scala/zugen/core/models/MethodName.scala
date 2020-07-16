package zugen.core.models

final case class MethodName(value: String) extends AnyVal {

  override def toString: String = value
}

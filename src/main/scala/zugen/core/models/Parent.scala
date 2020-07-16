package zugen.core.models

final case class Parent(tpe: ParentType) {
  override def toString: String = tpe.typeName
}

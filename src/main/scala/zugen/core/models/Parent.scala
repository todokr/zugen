package zugen.core.models

case class Parent(tpe: ParentType) {
  override def toString: String = tpe.typeName
}

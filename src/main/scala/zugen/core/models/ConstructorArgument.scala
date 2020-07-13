package zugen.core.models

/** An argument of constructor */
final case class ConstructorArgument(name: ConstructorArgumentName, tpe: ConstructorArgumentType) {
  override def toString: String = s"$name: ${tpe.typeName}"
}

package zugen.core.models

import zugen.core.models.Constructor.Argument

final case class Constructor(args: Seq[Argument])

object Constructor {

  final case class Argument(name: ArgumentName, tpe: ArgumentType) {
    override def toString: String = s"$name: ${tpe.typeName}"
  }
  final case class ArgumentName(value: String) extends AnyVal {
    override def toString: String = value
  }
  final case class ArgumentType(typeName: String, typeArgs: Seq[String], pkg: Package)
}

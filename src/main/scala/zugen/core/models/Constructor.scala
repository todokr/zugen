package zugen.core.models

import zugen.core.models.Constructor.Arg

case class Constructor(args: Seq[Arg])

object Constructor {

  case class Arg(name: ArgName, tpe: Tpe) {
    override def toString: String = s"$name: ${tpe.typeName}"
  }
  case class ArgName(value: String) extends AnyVal {
    override def toString: String = value
  }
  case class Tpe(typeName: String, typeArgs: Seq[String], pkg: Package)
}

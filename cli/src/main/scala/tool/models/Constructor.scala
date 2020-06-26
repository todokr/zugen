package tool.models

import tool.models.Constructor.Arg

/**
  * クラスのコンストラクタ
  */
case class Constructor(args: Seq[Arg])

object Constructor {

  case class Arg(name: ArgName, tpe: Tpe) {
    override def toString: String = s"${name.value}: ${tpe.typeName}"
  }
  case class ArgName(value: String) extends AnyVal
  case class Tpe(typeName: String, pkg: Package)
}

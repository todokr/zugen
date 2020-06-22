package tool.models

import tool.models.Constructor.Arg

/**
  * クラスのコンストラクタ
  */
case class Constructor(args: Seq[Arg])

object Constructor {

  case class Arg(name: ArgName, typeName: TypeName) {
    override def toString: String = s"${name.value}: ${typeName.value}"
  }
  case class ArgName(value: String) extends AnyVal
  case class TypeName(value: String) extends AnyVal
}

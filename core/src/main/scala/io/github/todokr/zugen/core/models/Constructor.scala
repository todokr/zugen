package io.github.todokr.zugen.core.models

import io.github.todokr.zugen.core.models.Constructor.Arg

/**
  * クラスのコンストラクタ
  */
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

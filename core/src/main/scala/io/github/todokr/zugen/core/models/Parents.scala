package io.github.todokr.zugen.core.models

import io.github.todokr.zugen.core.models.Parents.Parent

/**
  * クラスやトレイトの親
  */
case class Parents(elms: Seq[Parent])

object Parents {

  case class Parent(tpe: Parent.Tpe) {
    override def toString: String = tpe.typeName
  }

  object Parent {
    case class Tpe(typeName: String, typeArgs: Seq[String], pkg: Package)
  }
}

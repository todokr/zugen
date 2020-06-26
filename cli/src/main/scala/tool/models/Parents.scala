package tool.models

import tool.models.Parents.Parent

/**
  * クラスやトレイトの親
  */
case class Parents(elms: Seq[Parent])

object Parents {

  case class Parent(tpe: Parent.Tpe) {
    override def toString: String = tpe.typeName
  }

  object Parent {
    case class Tpe(typeName: String, pkg: Package)
  }
}

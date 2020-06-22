package tool.models

import tool.models.Parents.Parent

/**
  * クラスやトレイトの親
  */
case class Parents(elms: Seq[Parent])

object Parents {

  case class Parent(typeName: Parent.TypeName) {
    override def toString: String = typeName.value
  }

  object Parent {
    case class TypeName(value: String) extends AnyVal
  }
}

package zugen.core.models

import zugen.core.models.Parents.Parent

/**
  * parents of class etc.
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

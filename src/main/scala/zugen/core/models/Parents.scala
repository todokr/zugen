package zugen.core.models

import zugen.core.models.Parents.Parent
import zugen.core.models.Parents.Parent.ParentType

/** parents of class etc. */
case class Parents(elms: Seq[Parent])

object Parents {

  case class Parent(tpe: ParentType) {
    override def toString: String = tpe.typeName
  }

  object Parent {
    case class ParentType(typeName: String, typeArgs: Seq[String], pkg: Package)
  }
}

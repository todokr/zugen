package tool.models

import tool.models.Definitions.DefinitionBlock
import tool.models.References.Reference

/**
  * クラスやトレイトが参照する定義
  *
  * @example `case class(a: A) extends B` の場合、 `Seq(Property(A), Inheritance(B))`
  */
case class References(elms: Seq[Reference])

object References {

  sealed trait Reference {
    def definition: DefinitionBlock
  }

  object Reference {

    case class Inheritance(definition: DefinitionBlock) extends Reference {
      override def toString: String = s"[inherit] ${definition.pkg}.${definition.name.value}"
    }

    case class Property(definition: DefinitionBlock) extends Reference {
      override def toString: String = s"[prop] ${definition.pkg}.${definition.name.value}"
    }
  }
}

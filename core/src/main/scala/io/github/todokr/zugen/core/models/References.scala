package io.github.todokr.zugen.core.models

import io.github.todokr.zugen.core.models.Definitions.DefinitionBlock
import io.github.todokr.zugen.core.models.References.Reference

/**
  * クラスやトレイトが参照する定義
  */
case class References(elms: Seq[Reference])

object References {

  sealed trait Reference

  /**
    * プロジェクトのパッケージの参照
    */
  sealed trait InternalReference extends Reference {
    def definition: DefinitionBlock
  }

  object InternalReference {
    final case class InternalInheritance(definition: DefinitionBlock) extends InternalReference
    final case class InternalProperty(definition: DefinitionBlock) extends InternalReference
  }

  /**
    * プロジェクト外のパッケージの参照
    */
  sealed trait ExternalReference extends Reference {
    def pkg: Package
    def typeName: String
  }

  object ExternalReference {
    final case class ExternalInheritance(pkg: Package, typeName: String) extends ExternalReference
    final case class ExternalProperty(pkg: Package, typeName: String) extends ExternalReference
  }
}

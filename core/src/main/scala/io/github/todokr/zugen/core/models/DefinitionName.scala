package io.github.todokr.zugen.core.models

/**
  * クラスやトレイトなどの名称
  */
case class DefinitionName(value: String) extends AnyVal {

  override def toString: String = value
}

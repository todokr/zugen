package io.github.todokr.zugen.core.models

import io.github.todokr.zugen.core.models.Modifiers.{AccessibilityModifierElement, ModifierElement}

/**
  * クラスやトレイトの修飾子
  */
case class Modifiers(elems: Seq[ModifierElement]) {

  /**
    * アクセス性に関する修飾子を取得する
    */
  def accessibility: String =
    elems
      .find(_.isInstanceOf[AccessibilityModifierElement])
      .map(_.toString)
      .getOrElse("Public")
}

object Modifiers {
  sealed trait ModifierElement

  object ModifierElement {
    case object Sealed extends ModifierElement
    case object Final extends ModifierElement
    case object Case extends ModifierElement
  }

  /**
    * アクセス性の修飾子
    */
  sealed trait AccessibilityModifierElement extends ModifierElement
  object AccessibilityModifierElement {
    case object Private extends AccessibilityModifierElement
    case object Protected extends AccessibilityModifierElement
    case class PackagePrivate(packageName: String) extends AccessibilityModifierElement
    case class PackageProtected(packageName: String) extends AccessibilityModifierElement
  }
}

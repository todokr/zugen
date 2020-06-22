package tool.models

import tool.models.Modifiers.ModifierElement
import tool.models.Modifiers.ModifierElement.AccessibilityModifierElement

/**
  * クラスやトレイトの修飾子
  */
case class Modifiers(elems: Seq[ModifierElement]) {

  /**
    * 可視性に関する修飾子を取得する
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

    sealed trait AccessibilityModifierElement extends ModifierElement
    case object Private extends AccessibilityModifierElement
    case object Protected extends AccessibilityModifierElement

    case class PackagePrivate(packageName: String) extends AccessibilityModifierElement {

      override def toString: String = s"Private[${packageName}]"
    }

    case class PackageProtected(packageName: String) extends AccessibilityModifierElement {
      override def toString: String = s"Protected[${packageName}]"
    }
  }
}

package zugen.core.models

import zugen.core.models.Modifiers.{AccessibilityModifierElement, ModifierElement}

/** modifiers for class etc. */
case class Modifiers(elems: Seq[ModifierElement]) {

  /** modifiers related to accessibility */
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

  sealed trait AccessibilityModifierElement extends ModifierElement
  object AccessibilityModifierElement {
    case object Private extends AccessibilityModifierElement
    case object Protected extends AccessibilityModifierElement
    case class PackagePrivate(packageName: String) extends AccessibilityModifierElement
    case class PackageProtected(packageName: String) extends AccessibilityModifierElement
  }
}

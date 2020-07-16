package zugen.core.models

import zugen.core.models.Modifier.AccessibilityModifier

/** Modifiers for class etc. */
final case class Modifiers(elms: Seq[Modifier]) {

  def accessibility: String =
    elms
      .collectFirst { case m: AccessibilityModifier => m.toString }
      .getOrElse("Public")
}

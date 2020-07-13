package zugen.core.models

/** An modifier like sealed, final, etc. */
sealed trait Modifier

object Modifier {
  case object Sealed extends Modifier
  case object Final extends Modifier
  case object Case extends Modifier

  /** An modifier connected to accessibility */
  sealed trait AccessibilityModifier extends Modifier
  object AccessibilityModifier {
    case object Private extends AccessibilityModifier
    case object Protected extends AccessibilityModifier
    case class PackagePrivate(packageName: String) extends AccessibilityModifier
    case class PackageProtected(packageName: String) extends AccessibilityModifier
  }
}

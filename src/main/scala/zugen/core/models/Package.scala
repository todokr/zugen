package zugen.core.models

import scala.util.chaining._

case class Package(ids: Seq[QualId]) {

  def isInPackage(other: Package): Boolean =
    toString.startsWith(other.toString)

  override def toString: String = {
    val expression = ids.mkString(".")
    if (expression.trim().isEmpty) "DEFAULT_PACKAGE"
    else expression
  }
}

object Package extends (String => Package) {

  def apply(packageStr: String): Package =
    packageStr
      .split('.')
      .toIndexedSeq
      .map(QualId)
      .pipe(Package(_))

  val unknown: Package = Package(Seq.empty)
}

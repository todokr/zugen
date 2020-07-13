package zugen.core.models

import scala.util.chaining._

case class Packages(elms: Seq[Package]) {

  def isInPackage(other: Packages): Boolean =
    toString.startsWith(other.toString)

  override def toString: String = elms.mkString(".")
}

object Packages extends (String => Packages) {

  def apply(packageStr: String): Packages =
    packageStr
      .split('.')
      .toIndexedSeq
      .map(Package)
      .pipe(Packages(_))

  val unknown: Packages = Packages(Seq.empty)
}

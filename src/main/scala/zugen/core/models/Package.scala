package zugen.core.models

import scala.util.chaining._
import Package.PackageElement

case class Package(elems: Seq[PackageElement]) {

  def isInPackage(other: Package): Boolean =
    toString.startsWith(other.toString)

  override def toString: String = elems.mkString(".")
}

object Package extends (String => Package) {
  case class PackageElement(value: String) extends AnyVal {
    override def toString: String = value
  }

  def apply(packageStr: String): Package =
    packageStr.split('.').toIndexedSeq.map(Package.PackageElement).pipe(Package(_))
}

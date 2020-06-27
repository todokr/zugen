package tool.models

import scala.util.chaining._
import tool.models.Package.PackageElement

/**
  * パッケージ
  */
case class Package(elems: Seq[PackageElement]) {

  /**
    * 指定されたパッケージに含まれるならtrue
    */
  def isInPackage(other: Package): Boolean =
    toString.startsWith(other.toString)

  override def toString: String = elems.mkString(".")
}

object Package extends (String => Package) {
  case class PackageElement(value: String) extends AnyVal {
    override def toString: String = value
  }

  def apply(packageStr: String): Package =
    packageStr.split('.').map(Package.PackageElement).pipe(Package(_))
}

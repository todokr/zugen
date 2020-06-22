package tool.models

import tool.models.Package.PackageElement

/**
  * パッケージ
  */
case class Package(elems: Seq[PackageElement]) {

  /**
    * 指定されたパッケージのいずれかに含まれるならtrue
    */
  def isInAnyPackage(packageNames: Seq[String]): Boolean =
    packageNames.exists(p => toString.startsWith(p))

  override def toString: String = elems.map(_.value).mkString(".")
}

object Package extends (String => Package) {
  case class PackageElement(value: String) extends AnyVal

  def apply(packageStr: String): Package = {
    val elms = packageStr.split('.').map(Package.PackageElement)
    Package(elms)
  }
}

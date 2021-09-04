package zugen.core.config

import java.net.URL

final case class Config(
  documentsToGenerate: DocumentsToGenerate,
  domainPackages: Seq[DomainPackageName],
  domainObjectExcludePatterns: Seq[String],
  methodInvocationStartingPackage: Option[MethodInvocationRootPackage],
  documentPath: DocumentPath,
  githubBaseUrl: Option[URL]
) {
  override def toString: String =
    s"""
       |documentsToGenerate: ${documentsToGenerate.genDocTypes.map(_.code).mkString(", ")},
       |domainPackages: ${domainPackages.map(_.value).mkString(", ")}
       |domainObjectExcludePatterns: ${domainObjectExcludePatterns.mkString(", ")}
       |methodInvocationStartingPackage: ${methodInvocationStartingPackage.map(_.value).getOrElse("-")}
       |documentPath: ${documentPath.value}
       |githubBaseUrl: ${githubBaseUrl.map(_.toString).getOrElse("-")}""".stripMargin
}

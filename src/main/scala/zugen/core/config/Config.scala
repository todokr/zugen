package zugen.core.config

import java.net.URL

final case class Config(
  documentsToGenerate: DocumentsToGenerate,
  domainPackages: Seq[DomainPackageName],
  domainObjectExcludePatterns: Seq[String],
  methodInvocationStartingPackage: Option[MethodInvocationRootPackage],
  documentPath: DocumentPath,
  classesPath: ClassesPath,
  githubBaseUrl: Option[URL]
)

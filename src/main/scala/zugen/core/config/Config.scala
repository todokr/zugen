package zugen.core.config

final case class Config(
  documentsToGenerate: DocumentsToGenerate,
  domainPackages: Seq[DomainPackageName],
  domainObjectExcludePatterns: Seq[String],
  methodInvocationStartingPackage: Option[MethodInvocationRootPackage],
  documentPath: DocumentPath,
  classesPath: ClassesPath
)

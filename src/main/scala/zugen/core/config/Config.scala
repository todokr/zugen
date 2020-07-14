package zugen.core.config

final case class Config(
  documentsToGenerate: DocumentsToGenerate,
  domainPackages: Seq[DomainPackageName],
  domainObjectExcludePatterns: Seq[String],
  methodInvocationStartingPackage: Option[MethodInvocationStartingPackage],
  documentPath: DocumentPath,
  classesPath: ClassesPath
)

case class MethodInvocationStartingPackage(value: String)

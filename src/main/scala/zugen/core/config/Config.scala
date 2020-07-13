package zugen.core.config

final case class Config(
  documentsToGenerate: DocumentsToGenerate,
  domainPackages: Seq[DomainPackageName],
  domainObjectExcludePatterns: Seq[String],
  documentPath: DocumentPath,
  classesPath: ClassesPath
)

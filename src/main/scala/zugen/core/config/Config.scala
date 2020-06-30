package zugen.core.config

import java.nio.file.{Files, Path, Paths}

final case class Config(
  classesPath: ClassesPath,
  domainPackages: Seq[DomainPackageName],
  documentsToGenerate: DocumentsToGenerate,
  documentPath: DocumentPath
)

/**
  * classes directory path of target project
  */
final case class ClassesPath(value: Path) extends AnyVal

object ClassesPath {
  def apply(value: String): ClassesPath = ClassesPath(Paths.get(value))
}

/**
  * domain object's package name
  * only classes/traits/objects included in these package are shown in generated document
  */
final case class DomainPackageName(value: String) extends AnyVal
final case class DocumentsToGenerate(genDocTypes: Seq[GenDocumentType])

/**
  * directory path of documents to generate
  */
final case class DocumentPath(value: Path) extends AnyVal {
  def exists: Boolean = Files.exists(value)
}
object DocumentPath {
  def apply(value: String): DocumentPath = DocumentPath(Paths.get(value))
}

sealed trait GenDocumentType

object GenDocumentType {

  /**
    * table of domain objects
    */
  case object GenDomainObjectTable extends GenDocumentType

  /**
    * relation diagram of domain objects
    */
  case object GenDomainRelationDiagram extends GenDocumentType
}

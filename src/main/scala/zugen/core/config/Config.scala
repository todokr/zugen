package zugen.core.config

import java.nio.file.{Files, Path, Paths}

final case class Config(
  documentsToGenerate: DocumentsToGenerate,
  domainPackages: Seq[DomainPackageName],
  domainObjectExcludePatterns: Seq[String],
  documentPath: DocumentPath,
  classesPath: ClassesPath
)

final case class DocumentsToGenerate(genDocTypes: Seq[GenDocumentType])
sealed trait GenDocumentType

object GenDocumentType {

  def from(code: String): Option[GenDocumentType] = {
    code match {
      case "domain-object-table"     => Some(GenDomainObjectTable)
      case "domain-relation-diagram" => Some(GenDomainRelationDiagram)
      case other =>
        println(s"${Console.YELLOW}[WARN]${Console.RESET} no such document type: $other")
        None
    }
  }
  val values: Seq[GenDocumentType] = Seq(GenDomainObjectTable, GenDomainRelationDiagram)

  /** table of domain objects */
  case object GenDomainObjectTable extends GenDocumentType

  /** relation diagram of domain objects */
  case object GenDomainRelationDiagram extends GenDocumentType
}

/** domain object's package name
  *
  * only classes/traits/objects included in these package are shown in generated document
  */
final case class DomainPackageName(value: String) extends AnyVal

/** classes directory path of target project */
final case class ClassesPath(value: Path) extends AnyVal

object ClassesPath extends (String => ClassesPath) {
  def apply(value: String): ClassesPath = ClassesPath(Paths.get(value))
}

/** directory path of documents to generate */
final case class DocumentPath(value: Path) extends AnyVal {
  def exists: Boolean = Files.exists(value)
}
object DocumentPath extends (String => DocumentPath) {
  def apply(value: String): DocumentPath = DocumentPath(Paths.get(value))
}

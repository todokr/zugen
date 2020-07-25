package zugen.core.document

import scala.util.chaining._

import zugen.core.config.Config
import zugen.core.document.DomainObjectTableDocument.DomainObjectTableRow
import zugen.core.models.{DocumentMaterials, FileName, Package, TemplateDefinitionName}

/** domain object table */
final case class DomainObjectTableDocument(rows: Seq[DomainObjectTableRow]) extends Document {

  override val docCode: String = "domain-object-table"
  override val docName: String = "Domain Object Table"
}

object DomainObjectTableDocument {

  def of(documentmaterials: DocumentMaterials, config: Config): DomainObjectTableDocument = {
    val domainPackages = config.domainPackages.map(n => Package(n.value))
    documentmaterials.elms.collect {
      case elm if elm.templateDefinition.isInAnyPackage(domainPackages) =>
        DomainObjectTableRow(
          pkg = elm.templateDefinition.pkg,
          name = elm.templateDefinition.name,
          scaladoc = elm.templateDefinition.scaladoc.map(_.content).getOrElse(""),
          fileName = elm.templateDefinition.fileName,
          fileUrl = config.githubBaseUrl.map(baseUrl => s"$baseUrl/${elm.templateDefinition.fileName}")
        )
    }.pipe(DomainObjectTableDocument(_))
  }

  final case class DomainObjectTableRow(
    pkg: Package,
    name: TemplateDefinitionName,
    scaladoc: String,
    fileName: FileName,
    fileUrl: Option[String]
  )
}

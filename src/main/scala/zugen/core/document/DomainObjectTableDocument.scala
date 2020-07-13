package zugen.core.document

import scala.util.chaining._

import zugen.core.config.Config
import zugen.core.document.DomainObjectTableDocument.DomainObjectTableRow
import zugen.core.models.{DocumentMaterials, FileName, Packages, TemplateName}

/** domain object table */
final case class DomainObjectTableDocument(rows: Seq[DomainObjectTableRow]) extends Document {

  override val docCode: String = "domain-object-table"
  override val docName: String = "Domain Object Table"
}

object DomainObjectTableDocument {

  def of(documentmaterials: DocumentMaterials, config: Config): DomainObjectTableDocument = {
    val domainPackages = config.domainPackages.map(n => Packages(n.value))
    documentmaterials.elms.collect {
      case elm if elm.template.isInAnyPackage(domainPackages) =>
        DomainObjectTableRow(
          pkg = elm.template.pkg,
          name = elm.template.name,
          scaladoc = elm.scaladoc.map(_.content).getOrElse(""),
          fileName = elm.template.fileName
        )
    }.pipe(DomainObjectTableDocument(_))
  }

  final case class DomainObjectTableRow(
    pkg: Packages,
    name: TemplateName,
    scaladoc: String,
    fileName: FileName
  )
}

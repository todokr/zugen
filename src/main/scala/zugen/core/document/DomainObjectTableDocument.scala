package zugen.core.document

import scala.util.chaining._

import zugen.core.config.Config
import zugen.core.document.DomainObjectTableDocument.DomainObjectTableRow
import zugen.core.models.{DefinitionName, DocumentMaterial, FileName, Package}

/** domain object table */
final case class DomainObjectTableDocument(rows: Seq[DomainObjectTableRow]) extends Document {

  override val docCode: String = "domain-object-table"
  override val docName: String = "Domain Object Table"
}

object DomainObjectTableDocument {

  def of(documentMaterial: DocumentMaterial, config: Config): DomainObjectTableDocument = {
    val domainPackages = config.domainPackages.map(n => Package(n.value))
    documentMaterial.elms.collect {
      case elm if elm.definition.isInAnyPackage(domainPackages) =>
        DomainObjectTableRow(
          pkg = elm.definition.pkg,
          name = elm.definition.name,
          scaladoc = elm.scaladoc.map(_.content).getOrElse(""),
          fileName = elm.definition.fileName
        )
    }.pipe(DomainObjectTableDocument(_))
  }

  final case class DomainObjectTableRow(
    pkg: Package,
    name: DefinitionName,
    scaladoc: String,
    fileName: FileName
  )
}

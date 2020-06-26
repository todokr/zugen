package tool.document

import java.nio.file.Files

import tool.Config
import tool.Config.GenDocumentType
import tool.Config.GenDocumentType.{GenDomainObjectTable, GenDomainPackageRelationDiagram}
import tool.document.Document.{DomainObjectTableDoc, DomainRelationDiagramDoc}
import tool.document.DocumentWriter.WrittenDocumentPath
import tool.models.DocumentMaterial

object DocumentWriterImpl extends DocumentWriter {

  override def write(
      documentMaterial: DocumentMaterial,
      documentType: GenDocumentType,
      config: Config): WrittenDocumentPath = {

    val doc = documentType match {
      case GenDomainObjectTable            => DomainObjectTableDoc.of(documentMaterial)
      case GenDomainPackageRelationDiagram => DomainRelationDiagramDoc.of(documentMaterial, config)
    }

    val filePath = config.documentPath.value.resolve(s"${doc.docName}.html")

    Files.write(filePath, doc.serialize)
    WrittenDocumentPath(filePath)
  }
}

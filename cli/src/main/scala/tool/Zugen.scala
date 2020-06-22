package tool

import java.nio.file.Files

import tool.Config.DocumentType.{DomainObjectTable, DomainPackageRelationDiagram}
import tool.Config.{DocumentPath, DocumentType}
import tool.Document.{DomainObjectTableDoc, DomainPackageRelationDiagramDoc}
import tool.models.DocumentWriter.WrittenDocumentPath
import tool.models.{DocumentMaterial, DocumentWriter}

object Zugen {

  def run(config: Config): Unit = {
    implicit val c: Config = config

    val textDocs = TextDocLoader.load(config.targetProjectRootPath)
    val definitions =
      DefinitionExtractor
        .extractDefinitions(textDocs)
        .filterPackages(config.targetPackageNames)
    val scaladocs = ScaladocExtractor.extractScaladocs(textDocs)
    val documentMaterial = definitions.mergeWithScaladoc(scaladocs)

    implicit val writer: DocumentWriter = DocumentWriterImpl
    config.documentsToGenerate.docTypes.foreach { docType =>
      val writtenDocumentPath = documentMaterial.writeDocument(docType)
      println(s"${docType} wrote: ${writtenDocumentPath.value.toAbsolutePath}")
    }
  }
}

object DocumentWriterImpl extends DocumentWriter {

  override def write(
      documentMaterial: DocumentMaterial,
      documentType: DocumentType,
      documentPath: DocumentPath): WrittenDocumentPath = {

    val doc = documentType match {
      case DomainObjectTable            => DomainObjectTableDoc.of(documentMaterial)
      case DomainPackageRelationDiagram => DomainPackageRelationDiagramDoc.of(documentMaterial)
    }

    val filePath = documentPath.value.resolve(s"$documentType.html")

    Files.write(filePath, doc.serialize)
    WrittenDocumentPath(filePath)
  }
}

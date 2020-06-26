package tool

import java.nio.file.Files

import tool.Config.GenDocumentType.{GenDomainObjectTable, GenDomainPackageRelationDiagram}
import tool.Config.GenDocumentType
import tool.document.Document.{DomainObjectTableDoc, DomainRelationDiagramDoc}
import tool.models.DocumentWriter.WrittenDocumentPath
import tool.models.{DocumentMaterial, DocumentWriter}

object Zugen {

  def run(config: Config): Unit = {
    implicit val c: Config = config

    val textDocs = TextDocLoader.load(config.targetProjectRootPath)
    val definitions =
      DefinitionExtractor
        .extractDefinitions(textDocs)
    val scaladocs = ScaladocExtractor.extractScaladocs(textDocs)
    val documentMaterial = definitions.mergeWithScaladoc(scaladocs)

    implicit val writer: DocumentWriter = DocumentWriterImpl
    config.documentsToGenerate.genDocTypes.foreach { docType =>
      val writtenDocumentPath = documentMaterial.writeDocument(docType)
      println(s"${docType} wrote: ${writtenDocumentPath.value.toAbsolutePath}")
    }
  }
}

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

package zugen.core

import java.nio.file.{Files, Path}
import java.time.{Clock, LocalDateTime}

import zugen.core.config.Config
import zugen.core.config.GenDocumentType.{GenDomainObjectTable, GenDomainRelationDiagram}
import zugen.core.document.Document.{DomainObjectTableDoc, DomainRelationDiagramDoc}
import zugen.core.document.{DocumentWriter, HtmlDocumentWriter}
import zugen.core.loader.{MaterialLoader, SemanticDBMaterialLoader}

object Zugen {

  val materialLoader: MaterialLoader = SemanticDBMaterialLoader
  val documentWriter: DocumentWriter = HtmlDocumentWriter
  val clock: Clock = Clock.systemDefaultZone()

  def generateDocs(config: Config): GeneratedDocumentPath = {
    val generatedAt = LocalDateTime.now(clock)
    if (!config.documentPath.exists) {
      Files.createDirectories(config.documentPath.value)
    }

    val documentMaterial = materialLoader.load(config)
    val zugenDocuments = config.documentsToGenerate.genDocTypes.map {
      case GenDomainObjectTable     => DomainObjectTableDoc.of(documentMaterial, config)
      case GenDomainRelationDiagram => DomainRelationDiagramDoc.of(documentMaterial, config)
    }
    val generatedDocumentPaths = zugenDocuments.map(documentWriter.writeDocument(_, generatedAt, config))
    val indexDocument = documentWriter.writeIndexDocument(generatedDocumentPaths, generatedAt, config)

    GeneratedDocumentPath(
      index = indexDocument.path,
      pages = generatedDocumentPaths.map(_.path.toAbsolutePath)
    )
  }

  case class GeneratedDocumentPath(index: Path, pages: Seq[Path])
}

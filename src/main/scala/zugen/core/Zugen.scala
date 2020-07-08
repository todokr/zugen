package zugen.core

import java.nio.file.{Files, Path}
import java.time.{Clock, LocalDateTime}

import zugen.core.config.Config
import zugen.core.config.GenDocumentType.{GenDomainObjectTable, GenDomainRelationDiagram, GenUsecaseTable}
import zugen.core.document.{
  DocumentWriter,
  DomainObjectTableDocument,
  DomainRelationDiagramDocument,
  HtmlDocumentWriter,
  UsecaseTableDocument
}
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
      case GenDomainObjectTable     => DomainObjectTableDocument.of(documentMaterial, config)
      case GenDomainRelationDiagram => DomainRelationDiagramDocument.of(documentMaterial, config)
      case GenUsecaseTable          => UsecaseTableDocument.of(documentMaterial, config)
      case other                    => throw new Exception(s"Unknown document type: $other")
    }
    val generatedDocumentPaths = zugenDocuments.map(documentWriter.writeDocument(_, generatedAt, config))
    val indexDocument = documentWriter.writeIndexDocument(generatedDocumentPaths, generatedAt, config)

    GeneratedDocumentPath(
      index = indexDocument.path.toAbsolutePath,
      pages = generatedDocumentPaths.map(_.path.toAbsolutePath)
    )
  }

  case class GeneratedDocumentPath(index: Path, pages: Seq[Path])
}

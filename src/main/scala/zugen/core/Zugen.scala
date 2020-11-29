package zugen.core

import java.io.File
import java.nio.file.{Files, Path}
import java.time.{Clock, LocalDateTime}

import scala.jdk.CollectionConverters.asScalaIteratorConverter

import zugen.core.config.{ClassesPath, Config}
import zugen.core.config.GenDocumentType.{GenDomainObjectTable, GenDomainRelationDiagram, GenMethodInvocationDiagram}
import zugen.core.document.{
  DocumentWriter,
  DomainObjectTableDocument,
  DomainRelationDiagramDocument,
  HtmlDocumentWriter,
  MethodInvocationDiagramDocument
}
import zugen.core.loader.{MaterialLoader, SemanticDBMaterialLoader}

object Zugen {

  val materialLoader: MaterialLoader = SemanticDBMaterialLoader
  val documentWriter: DocumentWriter = HtmlDocumentWriter
  val clock: Clock = Clock.systemDefaultZone()

  def generateDocs(config: Config, projectStructure: ProjectStructure): GeneratedDocumentPath = {
    val generatedAt = LocalDateTime.now(clock)
    if (!config.documentPath.exists) {
      Files.createDirectories(config.documentPath.value)
    }
    val classesPaths = config.classesPath +: projectStructure.dependencies.map(_.classes)

    val documentMaterial = materialLoader.load(classesPaths)
    val zugenDocuments = config.documentsToGenerate.genDocTypes.map {
      case GenDomainObjectTable       => DomainObjectTableDocument.of(documentMaterial, config)
      case GenDomainRelationDiagram   => DomainRelationDiagramDocument.of(documentMaterial, config)
      case GenMethodInvocationDiagram => MethodInvocationDiagramDocument.of(documentMaterial, config)
      case other                      => throw new Exception(s"Unknown document type: $other")
    }
    val generatedDocumentPaths = zugenDocuments.map(documentWriter.writeDocument(_, generatedAt, config))
    val indexDocument = documentWriter.writeIndexDocument(generatedDocumentPaths, generatedAt, config)

    GeneratedDocumentPath(
      index = indexDocument.path.toAbsolutePath,
      pages = generatedDocumentPaths.map(_.path.toAbsolutePath)
    )
  }

  case class ProjectDependency(
    base: File,
    classes: ClassesPath
  )
  case class ProjectStructure(dependencies: Seq[ProjectDependency])
  object ProjectStructure {
    def of(baseDirs: Seq[File]): ProjectStructure = {
      val dependencies = baseDirs.map { b =>
        val MaxDepth = 5
        val classesDir =
          Files.walk(b.toPath, MaxDepth)
            .iterator()
            .asScala
            .filter(p => Files.isDirectory(p) && p.getFileName.toString == "classes")
            .toSeq
            .headOption
            .getOrElse(throw new Exception(s"No classes dir found under $b, depth $MaxDepth"))
        ProjectDependency(b, ClassesPath(classesDir))
      }
      ProjectStructure(
        dependencies = dependencies
      )
    }
  }
  case class GeneratedDocumentPath(index: Path, pages: Seq[Path])
}

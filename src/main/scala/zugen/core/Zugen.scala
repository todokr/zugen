package zugen.core

import java.io.File
import java.nio.file.{Files, Path}
import java.time.{Clock, LocalDateTime}

import scala.jdk.CollectionConverters._

import zugen.core.config.Config
import zugen.core.config.GenDocumentType.{GenDomainObjectTable, GenDomainRelationDiagram, GenMethodInvocationDiagram}
import zugen.core.document._
import zugen.core.loader.{MaterialLoader, SemanticdbMaterialLoader}

object Zugen {

  val materialLoader: MaterialLoader = SemanticdbMaterialLoader
  val documentWriter: DocumentWriter = HtmlDocumentWriter
  val clock: Clock = Clock.systemDefaultZone()

  def generateDocs(config: Config, projectStructure: ProjectStructure): GeneratedDocumentPath = {
    val generatedAt = LocalDateTime.now(clock)
    if (!config.documentPath.exists) {
      Files.createDirectories(config.documentPath.value)
    }

    val documentMaterial = materialLoader.load(projectStructure)
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

  final case class ProjectId(value: String) extends AnyVal

  /** The whole project structure */
  final case class ProjectStructure(targetProject: TargetProject, dependencies: Seq[DependentProject]) {

    override def toString: String = {
      val formattedDependencies =
        if (dependencies.isEmpty) "" else dependencies.map(_.projectId.value).mkString("  ", "\n  ", "\n")
      s"""${Console.BOLD}${targetProject.projectId.value}${Console.RESET}
         |depends on:
         |$formattedDependencies""".stripMargin
    }

    val allProject: Seq[Project] = targetProject +: dependencies
  }

  sealed trait Project {
    def projectId: ProjectId
    def baseDir: File

    def buildArtifactDir: File =
      Files.walk(baseDir.toPath).iterator().asScala
        .collectFirst {
          case path if Files.isDirectory(path) && path.getFileName.endsWith("target") => path.toFile
        }.getOrElse(throw new TargetDirectoryNotFoundException(baseDir))
  }

  final class TargetDirectoryNotFoundException(baseDir: File)
      extends Exception(s"target directory not found under ${baseDir.getAbsolutePath}")

  /** the target project whose it's Zugen docs are to be created */
  final case class TargetProject(
    projectId: ProjectId,
    baseDir: File
  ) extends Project

  /** projects that the target project dependent */
  final case class DependentProject(
    projectId: ProjectId,
    baseDir: File
  ) extends Project

  final case class GeneratedDocumentPath(index: Path, pages: Seq[Path])
}

package zugen.core.loader

import java.nio.file.Files

import scala.jdk.CollectionConverters._
import scala.meta.internal.semanticdb
import scala.util.chaining._

import zugen.core.Zugen.ProjectStructure
import zugen.core.models.{DocumentMaterial, DocumentMaterials}

/** Loads source code information with Semanticdb */
object SemanticdbMaterialLoader extends MaterialLoader with SemanticdbTemplateExtractor {

  def load(project: ProjectStructure): DocumentMaterials = {
    val semanticdbFiles = project.allProject.flatMap { project =>
      val semanticdbDirs = Files.walk(project.buildArtifactDir.toPath)
        .iterator()
        .asScala
        .filter(f => f.getFileName.endsWith("semanticdb") && Files.isDirectory(f))

      semanticdbDirs.flatMap { dir =>
        Files.walk(dir)
          .iterator()
          .asScala
          .filter(_.getFileName.toString.endsWith(".semanticdb"))
          .toList
      }
    }

    val documents = semanticdbFiles.flatMap { file =>
      println(s"parsing: ${file.getFileName}")
      semanticdb.TextDocuments.parseFrom(Files.readAllBytes(file)).documents
    }

    val templateDefinitions = extractTemplateDefinitions(documents)
    templateDefinitions.elms
      .map { template =>
        val references = template.resolveReferences(templateDefinitions)
        DocumentMaterial(
          templateDefinition = template,
          references = references
        )
      }.pipe(DocumentMaterials)
  }
}

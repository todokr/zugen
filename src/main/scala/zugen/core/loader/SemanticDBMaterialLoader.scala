package zugen.core.loader

import java.nio.file.Files

import scala.jdk.CollectionConverters._
import scala.meta.internal.semanticdb
import scala.util.chaining._

import zugen.core.config.ClassesPath
import zugen.core.models.{DocumentMaterial, DocumentMaterials}

object SemanticDBMaterialLoader extends MaterialLoader with SemanticDBTemplateExtractor {

  def load(classesPaths: Seq[ClassesPath]): DocumentMaterials = {
    val semanticdbFiles = classesPaths.flatMap { classesPath =>
      val semanticdbRoot = classesPath.value.resolve("META-INF/semanticdb")

      Files.walk(semanticdbRoot)
        .iterator()
        .asScala
        .filter(_.getFileName.toString.endsWith(".semanticdb"))
        .toList
    }

    val documents = semanticdbFiles.flatMap { file =>
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

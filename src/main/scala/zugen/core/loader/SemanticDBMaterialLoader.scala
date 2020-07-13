package zugen.core.loader

import java.nio.file.{Files, Path}

import scala.jdk.CollectionConverters._
import scala.meta.internal.semanticdb
import scala.util.chaining._

import zugen.core.config.Config
import zugen.core.models.{DocumentMaterial, DocumentMaterials}

object SemanticDBMaterialLoader extends MaterialLoader with SemanticDBTemplateExtractor {

  def load(config: Config): DocumentMaterials = {
    val semanticdbRoot = config.classesPath.value.resolve("META-INF/semanticdb")
    if (!Files.exists(semanticdbRoot)) throw new SemanticdbDirectoryNotExistException(errorMsg(semanticdbRoot))

    val semanticdbFiles =
      Files.walk(semanticdbRoot)
        .iterator()
        .asScala
        .filter(_.getFileName.toString.endsWith(".semanticdb"))
        .toList

    val documents = semanticdbFiles.flatMap { file =>
      semanticdb.TextDocuments.parseFrom(Files.readAllBytes(file)).documents
    }

    val templateDefinitions = extractTemplates(documents)
    templateDefinitions.elms
      .map { template =>
        val references = template.resolveReferences(templateDefinitions)
        DocumentMaterial(
          templateDefinition = template,
          references = references
        )
      }.pipe(DocumentMaterials)
  }

  class SemanticdbDirectoryNotExistException(msg: String) extends Exception(msg)

  private def errorMsg(semanticdbRoot: Path): String =
    s"""
      |SemanticDB directory not found in ${Console.BOLD}$semanticdbRoot.${Console.RESET}
      |Make sure that semanticdb-scalac compiler plugin is added to target project.
      |see: https://scalameta.org/docs/semanticdb/guide.html#scalac-compiler-plugin
      |""".stripMargin
}

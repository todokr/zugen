package zugen.core.loader

import java.nio.file.{Files, Path}

import scala.jdk.CollectionConverters._
import scala.meta.internal.semanticdb

import zugen.core.config.Config
import zugen.core.models.DocumentMaterial

object SemanticDBMaterialLoader extends MaterialLoader with DefinitionExtractor with ScaladocExtractor {

  def load(config: Config): DocumentMaterial = {
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

    val definitions = extractDefinitions(documents)
    val scaladocs = extractScaladocs(documents)
    definitions.mergeWithScaladoc(scaladocs)
  }

  class SemanticdbDirectoryNotExistException(msg: String) extends Exception(msg)

  private def errorMsg(semanticdbRoot: Path): String =
    s"""
      |SemanticDB directory not found in ${Console.BOLD}$semanticdbRoot.${Console.RESET}
      |Make sure that semanticdb-scalac compiler plugin is added to target project.
      |see: https://scalameta.org/docs/semanticdb/guide.html#scalac-compiler-plugin
      |""".stripMargin
}

package tool

import java.nio.file.{Files, Paths}

import scala.meta.internal.semanticdb
import scala.meta.internal.semanticdb.TextDocument
import scala.collection.JavaConverters._

object TextDocLoader {

  /**
    * SemanticDBからTextDocumentをロードする。
    */
  def load(config: TextDocLoaderConfig): Seq[TextDocument] = {
    val semanticdbRoot =
      Paths.get(config.rootPath).resolve("META-INF").resolve("semanticdb")
    val semanticdbFiles = Files
      .walk(semanticdbRoot)
      .iterator()
      .asScala
      .filter(_.getFileName.toString.endsWith(".semanticdb"))
      .toList

    semanticdbFiles.flatMap { file =>
      semanticdb.TextDocuments.parseFrom(Files.readAllBytes(file)).documents
    }
  }

  final case class TextDocLoaderConfig(rootPath: String)
}

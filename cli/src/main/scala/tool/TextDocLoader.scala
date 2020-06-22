package tool

import java.nio.file.Files

import scala.collection.JavaConverters._
import scala.meta.internal.semanticdb
import scala.meta.internal.semanticdb.TextDocument

import tool.Config.TargetProjectRootPath

object TextDocLoader {

  /**
    * SemanticDBからTextDocumentをロードする。
    */
  def load(rootPath: TargetProjectRootPath): Seq[TextDocument] = {
    val semanticdbRoot =
      rootPath.value.resolve("META-INF").resolve("semanticdb")
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
}

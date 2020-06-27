package tool

import java.nio.file.{Files, Path}

import scala.jdk.CollectionConverters._
import scala.meta.internal.semanticdb
import scala.meta.internal.semanticdb.TextDocument

import tool.Config.ClassesPath

object TextDocLoader {

  /**
    * SemanticDBからTextDocumentをロードする
    */
  def load(classesPath: ClassesPath): Seq[TextDocument] = {
    val semanticdbRoot = classesPath.value.resolve("META-INF/semanticdb")
    if (!Files.exists(semanticdbRoot)) throw new SemanticdbDirectoryNotExistException(semanticdbRoot)

    val semanticdbFiles =
      Files.walk(semanticdbRoot)
        .iterator()
        .asScala
        .filter(_.getFileName.toString.endsWith(".semanticdb"))
        .toList

    semanticdbFiles.flatMap { file =>
      semanticdb.TextDocuments.parseFrom(Files.readAllBytes(file)).documents
    }
  }

  class SemanticdbDirectoryNotExistException(semanticdbRoot: Path)
      extends Exception(s"SemanticDB root: $semanticdbRoot")
}

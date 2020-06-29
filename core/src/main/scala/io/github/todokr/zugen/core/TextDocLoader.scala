package io.github.todokr.zugen.core

import java.nio.file.{Files, Path}

import scala.jdk.CollectionConverters._
import scala.meta.internal.semanticdb
import scala.meta.internal.semanticdb.TextDocument

import io.github.todokr.zugen.core.Config.ClassesPath

object TextDocLoader {

  /**
    * SemanticDBからTextDocumentをロードする
    */
  def load(classesPath: ClassesPath): Seq[TextDocument] = {
    val semanticdbRoot = classesPath.value.resolve("META-INF/semanticdb")
    if (!Files.exists(semanticdbRoot)) throw new SemanticdbDirectoryNotExistException(errorMsg(semanticdbRoot))

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

  class SemanticdbDirectoryNotExistException(msg: String) extends Exception(msg)

  private def errorMsg(semanticdbRoot: Path): String =
    s"""
      |SemanticDB directory not found in ${Console.BOLD}$semanticdbRoot.${Console.RESET}
      |Make sure that semanticdb-scalac compiler plugin is added to target project.
      |see: https://scalameta.org/docs/semanticdb/guide.html#scalac-compiler-plugin
      |""".stripMargin
}

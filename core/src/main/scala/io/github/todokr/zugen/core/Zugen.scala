package io.github.todokr.zugen.core

import java.nio.file.{Files, Path, Paths}

import io.github.todokr.zugen.core.document.{DocumentWriter, HtmlDocumentWriter}

object Zugen {

  /**
    * 各ドキュメントを生成し、生成したドキュメントのパスを表示する
    */
  def generateDoc(config: Config): GeneratedIndexHtml = {
    val documentWriter: DocumentWriter = HtmlDocumentWriter
    val textDocs = TextDocLoader.load(config.classesPath)
    val definitions = DefinitionExtractor.extractDefinitions(textDocs)
    val scaladocs = ScaladocExtractor.extractScaladocs(textDocs)
    val documentMaterial = definitions.mergeWithScaladoc(scaladocs)

    if (!config.documentPath.exists) {
      Files.createDirectories(config.documentPath.value)
    }

    val writtenDocumentPaths = documentWriter.write(documentMaterial, config)
    writtenDocumentPaths.foreach { path =>
      println(s"${Console.GREEN}Generated${Console.RESET}: ${path.value.toAbsolutePath}")
    }
    GeneratedIndexHtml(Paths.get("this/is/test"))
  }

  case class GeneratedIndexHtml(path: Path)
}

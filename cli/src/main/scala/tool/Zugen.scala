package tool

import java.nio.file.Files

import tool.document.{DocumentWriter, HtmlDocumentWriter}

object Zugen {

  /**
    * 各ドキュメントを生成し、生成したドキュメントのパスを表示する
    */
  def generateDoc(config: Config): Unit = {
    implicit val c: Config = config
    val documentWriter: DocumentWriter = HtmlDocumentWriter
    val textDocs = TextDocLoader.load(config.targetProjectRootPath)
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
  }
}

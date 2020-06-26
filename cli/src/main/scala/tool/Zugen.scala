package tool

import tool.document.{DocumentWriter, DocumentWriterImpl}

object Zugen {

  def generateDoc(config: Config): Unit = {
    implicit val c: Config = config
    implicit val documentWriter: DocumentWriter = DocumentWriterImpl

    val textDocs = TextDocLoader.load(config.targetProjectRootPath)
    val definitions =
      DefinitionExtractor
        .extractDefinitions(textDocs)
    val scaladocs = ScaladocExtractor.extractScaladocs(textDocs)
    val documentMaterial = definitions.mergeWithScaladoc(scaladocs)

    config.documentsToGenerate.genDocTypes.foreach { docType =>
      val writtenDocumentPath = documentMaterial.writeDocument(docType)
      println(s"${Console.GREEN}Generated${Console.RESET}: ${writtenDocumentPath.value.toAbsolutePath}")
    }
  }
}

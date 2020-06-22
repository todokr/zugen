package tool

import tool.TextDocLoader.TextDocLoaderConfig
import tool.models.DocumentIntermediate

final case class TargetPackageName(value: String) extends AnyVal

class CodeInfoLoader {

  def load(config: LoadConfig): Seq[DocumentIntermediate] = ???

  final case class LoadConfig(targetPackageNames: Seq[TargetPackageName])
}

object Main {

  def main(args: Array[String]): Unit = {
    val targetPackageNames = Seq("example.domain")
    args.toList match {
      case rootPath :: Nil =>
        val textDocLoaderConfig = TextDocLoaderConfig(rootPath)
        val textDocs = TextDocLoader.load(textDocLoaderConfig)

        val scaladocs = ScaladocExtractor.extractScaladocs(textDocs)
        val definitions = DefinitionExtractor.extractDefinitions(textDocs)

        val documentedDefinitions =
          definitions
            .filterPackages(targetPackageNames)
            .map { definition =>
              val references = definition.resolveReferences(definitions)
              models.DocumentIntermediate(
                definition = definition,
                scaladoc = scaladocs.findDocForDefinition(definition),
                references = references
              )
            }

        documentedDefinitions.foreach(println)
      case els =>
        sys.error(s"Expected <path>, obtained $els")
    }
  }
}

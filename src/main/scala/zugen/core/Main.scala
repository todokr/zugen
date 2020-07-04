package zugen.core

import scala.util.chaining._

import zugen.core.config.GenDocumentType.{GenDomainObjectTable, GenDomainRelationDiagram}
import zugen.core.config.{ClassesPath, Config, DocumentPath, DocumentsToGenerate, DomainPackageName}

/** entry point for debug */
object Main {

  def main(args: Array[String]): Unit = {

    args.toList match {
      case classesDir :: docsDir :: domainPkgs :: Nil =>
        val classesPath = ClassesPath(classesDir)
        val domainPackageNames = domainPkgs.split(",").toIndexedSeq.map(DomainPackageName)
        val documentsToGenerate = Seq(GenDomainObjectTable, GenDomainRelationDiagram).pipe(DocumentsToGenerate)
        val documentPath = DocumentPath(docsDir)
        val config = Config(classesPath, domainPackageNames, documentsToGenerate, documentPath)
        val generatedPath = Zugen.generateDocs(config)
        generatedPath.pages.foreach { page =>
          println(s"${scala.Console.GREEN}Generated${scala.Console.RESET}: $page")
        }
        println(s"${scala.Console.GREEN}Generated index page${scala.Console.RESET}: ${generatedPath.index}")
      case els =>
        sys.error(s"Expected <classesDir> <docsDir> <domainPkg[,domainPkg]*>, obtained $els")
    }
  }
}

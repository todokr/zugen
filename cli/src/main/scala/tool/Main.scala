package tool

import scala.util.chaining._

import tool.Config.GenDocumentType.{DomainObjectTableGen, DomainRelationDiagramGen}
import tool.Config.{DocumentPath, DocumentsToGenerate, TargetPackageName, TargetProjectRootPath}

object Main {

  def main(args: Array[String]): Unit = {

    args.toList match {
      case rootPath :: docPath :: Nil =>
        val targetProjectRootPath = TargetProjectRootPath(rootPath)
        val targetPackageNames = Seq("example.domain").map(TargetPackageName) // TODO
        val documentsToGenerate = Seq(DomainObjectTableGen, DomainRelationDiagramGen).pipe(DocumentsToGenerate)
        val documentPath = DocumentPath(docPath) // TODO
        val config = Config(targetProjectRootPath, targetPackageNames, documentsToGenerate, documentPath)
        Zugen.generateDoc(config)
      case els =>
        sys.error(s"Expected <rootPath> <docPath>, obtained $els")
    }
  }
}

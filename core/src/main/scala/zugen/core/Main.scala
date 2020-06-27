package zugen.core

import zugen.core.Config.GenDocumentType.{DomainObjectTableGen, DomainRelationDiagramGen}
import zugen.core.Config.{ClassesPath, DocumentPath, DocumentsToGenerate, TargetPackageName}
import scala.util.chaining._

/**
  * 開発時の動作確認用エントリーポイント
  */
object Main {

  def main(args: Array[String]): Unit = {

    args.toList match {
      case classesDir :: docsDir :: targetPkgs :: Nil =>
        val classesPath = ClassesPath(classesDir)
        val targetPackageNames = targetPkgs.split(",").toIndexedSeq.map(TargetPackageName)
        val documentsToGenerate = Seq(DomainObjectTableGen, DomainRelationDiagramGen).pipe(DocumentsToGenerate)
        val documentPath = DocumentPath(docsDir)
        val config = Config(classesPath, targetPackageNames, documentsToGenerate, documentPath)
        Zugen.generateDoc(config)
      case els =>
        sys.error(s"Expected <classesDir> <docsDir> <targetPkg[,targetPkg]*>, obtained $els")
    }
  }
}

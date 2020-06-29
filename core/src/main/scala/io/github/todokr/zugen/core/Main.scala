package io.github.todokr.zugen.core

import scala.util.chaining._

import io.github.todokr.zugen.core.Config.{ClassesPath, DocumentPath, DocumentsToGenerate, DomainPackageName}
import io.github.todokr.zugen.core.Config.GenDocumentType.{GenDomainObjectTable, GenDomainRelationDiagram}

/**
  * 開発時の動作確認用エントリーポイント
  */
object Main {

  def main(args: Array[String]): Unit = {

    args.toList match {
      case classesDir :: docsDir :: domainPkgs :: Nil =>
        val classesPath = ClassesPath(classesDir)
        val domainPackageNames = domainPkgs.split(",").toIndexedSeq.map(DomainPackageName)
        val documentsToGenerate = Seq(GenDomainObjectTable, GenDomainRelationDiagram).pipe(DocumentsToGenerate)
        val documentPath = DocumentPath(docsDir)
        val config = Config(classesPath, domainPackageNames, documentsToGenerate, documentPath)
        Zugen.generateDoc(config)
      case els =>
        sys.error(s"Expected <classesDir> <docsDir> <domainPkg[,domainPkg]*>, obtained $els")
    }
  }
}

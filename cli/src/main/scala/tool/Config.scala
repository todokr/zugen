package tool

import java.nio.file.{Files, Path}

import tool.Config.{DocumentPath, DocumentsToGenerate, TargetPackageName, TargetProjectRootPath}

final case class Config(
    targetProjectRootPath: TargetProjectRootPath,
    domainPackages: Seq[TargetPackageName],
    documentsToGenerate: DocumentsToGenerate,
    documentPath: DocumentPath
)

object Config {

  /**
    * ドキュメント生成対象プロジェクトのルートパス
    */
  final case class TargetProjectRootPath(value: Path) extends AnyVal

  /**
    * ドキュメント生成対象のパッケージ名
    */
  final case class TargetPackageName(value: String) extends AnyVal

  /**
    * 生成するドキュメント
    */
  final case class DocumentsToGenerate(genDocTypes: Seq[GenDocumentType])

  /**
    * 生成したドキュメントを配置するパス
    */
  final case class DocumentPath(value: Path) extends AnyVal {

    def exists: Boolean = Files.exists(value)
  }

  /**
    * ドキュメント種別
    */
  sealed trait GenDocumentType

  object GenDocumentType {
    case object GenDomainObjectTable extends GenDocumentType
    case object GenDomainPackageRelationDiagram extends GenDocumentType
  }

}

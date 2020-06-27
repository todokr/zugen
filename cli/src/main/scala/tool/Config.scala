package tool

import java.nio.file.{Files, Path, Paths}

import tool.Config.{DocumentPath, DocumentsToGenerate, TargetPackageName, TargetProjectRootPath}

final case class Config(
  targetProjectRootPath: TargetProjectRootPath, // ドキュメント生成対象プロジェクトのルートパス
  domainPackages: Seq[TargetPackageName], // ドキュメント生成対象のパッケージ名
  documentsToGenerate: DocumentsToGenerate, // 生成するドキュメント種別
  documentPath: DocumentPath // 生成したドキュメントを配置するパス
)

object Config {

  final case class TargetProjectRootPath(value: Path) extends AnyVal
  object TargetProjectRootPath {
    def apply(value: String): TargetProjectRootPath = TargetProjectRootPath(Paths.get(value))
  }
  final case class TargetPackageName(value: String) extends AnyVal
  final case class DocumentsToGenerate(genDocTypes: Seq[GenDocumentType])
  final case class DocumentPath(value: Path) extends AnyVal {
    def exists: Boolean = Files.exists(value)
  }
  object DocumentPath {
    def apply(value: String): DocumentPath = DocumentPath(Paths.get(value))
  }

  /**
    * ドキュメント種別
    */
  sealed trait GenDocumentType

  object GenDocumentType {
    case object DomainObjectTableGen extends GenDocumentType
    case object DomainRelationDiagramGen extends GenDocumentType
  }

}

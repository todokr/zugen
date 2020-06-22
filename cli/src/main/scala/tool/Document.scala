package tool

import java.nio.charset.StandardCharsets

import tool.Document.DomainObjectTableDoc.DomainObjectTableRow
import tool.models.{DefinitionName, DocumentMaterial, FileName, Package}

/**
  * 生成されるドキュメントのデータ
  */
sealed trait Document {

  def serialize: Array[Byte]
}

object Document {

  /**
    * ドメインオブジェクトの表
    */
  final case class DomainObjectTableDoc(rows: Seq[DomainObjectTableRow]) extends Document {

    override def serialize: Array[Byte] = {
      val html = views.html.DomainObjectTableDoc(this).body
      html.getBytes(StandardCharsets.UTF_8)
    }
  }

  object DomainObjectTableDoc {

    def of(documentMaterial: DocumentMaterial): DomainObjectTableDoc = {
      val rows = documentMaterial.elms.map { elm =>
        DomainObjectTableRow(
          pkg = elm.definition.pkg,
          name = elm.definition.name,
          scaladoc = elm.scaladoc.map(_.content).getOrElse("-"),
          fileName = elm.definition.fileName
        )
      }
      DomainObjectTableDoc(rows)
    }

    final case class DomainObjectTableRow(
        pkg: Package,
        name: DefinitionName,
        scaladoc: String,
        fileName: FileName
    )
  }

  /**
    * ドメインのパッケージ関連図
    */
  final case class DomainPackageRelationDiagramDoc() extends Document {
    override def serialize: Array[Byte] = ???
  }

  object DomainPackageRelationDiagramDoc {
    def of(documentMaterial: DocumentMaterial): Document = ???
  }
}

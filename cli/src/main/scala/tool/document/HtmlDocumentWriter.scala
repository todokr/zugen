package tool.document

import java.nio.charset.StandardCharsets
import java.nio.file.Files

import tool.Config
import tool.Config.GenDocumentType.{DomainObjectTableGen, DomainRelationDiagramGen}
import tool.document.Document.{DomainObjectTableDoc, DomainRelationDiagramDoc}
import tool.document.DocumentWriter.GeneratedDocumentPath
import tool.models.DocumentMaterial

/**
  * ドキュメントデータをHTMLとして書き出す
  */
object HtmlDocumentWriter extends DocumentWriter {

  override def write(material: DocumentMaterial, config: Config): Seq[GeneratedDocumentPath] = {

    val htmlWithName = config.documentsToGenerate.genDocTypes.map {
      case DomainObjectTableGen =>
        val doc = DomainObjectTableDoc.of(material)
        doc.docName -> views.html.domainobject.DomainObjectTable(doc).body
      case DomainRelationDiagramGen =>
        val doc = DomainRelationDiagramDoc.of(material, config)
        doc.docName -> views.html.domainobject.DomainRelationDiagram(doc).body
    }

    htmlWithName.map {
      case (docName, html) =>
        val filePath = config.documentPath.value.resolve(s"$docName.html")
        Files.write(filePath, html.getBytes(StandardCharsets.UTF_8))
        GeneratedDocumentPath(filePath)
    }
  }
}

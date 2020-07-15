package zugen.core.document

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.time.LocalDateTime

import zugen.core.config.Config

object HtmlDocumentWriter extends DocumentWriter {

  override def writeDocument(
    document: Document,
    generatedAt: LocalDateTime,
    config: Config
  ): GeneratedDocument = {
    val (doc, html) = document match {
      case doc: DomainObjectTableDocument =>
        doc -> views.html.DomainObjectTable(doc, generatedAt).body
      case doc: DomainRelationDiagramDocument =>
        val dot = escapeForDot(views.txt.ObjectRefs(doc.digraph).body)
        doc -> views.html.DomainRelationDiagram(dot, generatedAt).body
      case doc: MethodInvocationDiagramDocument =>
        val dot = escapeForDot(views.txt.InvocationTree().body)
        doc -> views.html.MethodInvocationDiagram(dot, generatedAt).body
    }
    val filePath = config.documentPath.value.resolve(s"${doc.docCode}.html")

    Files.write(filePath, html.getBytes(StandardCharsets.UTF_8))
    GeneratedDocument(doc.docName, filePath)
  }

  private def escapeForDot(body: String): String = body.replace("""`""", """\`""")

  /** generate index document for generated documents */
  override def writeIndexDocument(
    generatedDocuments: Seq[GeneratedDocument],
    generatedAt: LocalDateTime,
    config: Config
  ): GeneratedDocument = {
    val docCode = "index"
    val docName = "Index"
    val html = views.html.Index(generatedDocuments, generatedAt).body
    val filePath = config.documentPath.value.resolve(s"$docCode.html")
    Files.write(filePath, html.getBytes(StandardCharsets.UTF_8))
    GeneratedDocument(docName, filePath)
  }
}

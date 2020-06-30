package zugen.core.document

import java.time.LocalDateTime

import zugen.core.config.Config

trait DocumentWriter {

  /**
    * generate physical document files from zugen document
    */
  def writeDocument(
    document: Document,
    generatedAt: LocalDateTime,
    config: Config
  ): GeneratedDocument

  /**
    * generate index document for generated documents
    */
  def writeIndexDocument(
    generatedDocumentPaths: Seq[GeneratedDocument],
    generatedAt: LocalDateTime,
    config: Config
  ): GeneratedDocument
}

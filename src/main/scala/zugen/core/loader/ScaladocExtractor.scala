package zugen.core.loader

import scala.meta._
import scala.meta.contrib.{DocToken, ScaladocParser}
import scala.meta.internal.semanticdb.TextDocument
import scala.util.chaining._

import zugen.core.models.Scaladocs.ScaladocBlock
import zugen.core.models.{FileName, Scaladocs}

trait ScaladocExtractor {

  /**
    * extract scaladoc from SemanticDB TextDocument
    */
  def extractScaladocs(docs: Seq[TextDocument]): Scaladocs =
    docs.flatMap { doc =>
      val tokens =
        doc.text.tokenize.getOrElse(throw new Exception(s"failed to tokenize code: ${doc.uri}"))
      val fileName = FileName(doc.uri)
      val comments = tokens.collect { case c: Token.Comment => c }
      comments.flatMap { comment =>
        ScaladocParser
          .parseScaladoc(comment)
          .getOrElse(Seq.empty)
          .collect {
            case DocToken(_, _, Some(body)) =>
              ScaladocBlock(
                fileName = fileName,
                startLine = comment.pos.startLine,
                endLine = comment.pos.endLine,
                content = body
              )
          }
      }
    }.pipe(Scaladocs(_))

}

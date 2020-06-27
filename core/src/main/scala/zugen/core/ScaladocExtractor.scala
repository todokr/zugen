package zugen.core

import scala.meta.contrib.{DocToken, ScaladocParser}
import scala.meta.internal.semanticdb.TextDocument
import scala.meta._
import scala.util.chaining._

import zugen.core.models.Scaladocs.ScaladocBlock
import zugen.core.models.{FileName, Scaladocs}

object ScaladocExtractor {

  /**
    * コードからScaladoc形式のコメントブロックを抜き出す
    */
  def extractScaladocs(docs: Seq[TextDocument]): Scaladocs =
    docs.flatMap { doc =>
      val tokens =
        doc.text.tokenize.getOrElse(throw new Exception("failed to tokenize code"))
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

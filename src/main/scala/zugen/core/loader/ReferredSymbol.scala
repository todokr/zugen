package zugen.core.loader

import scala.util.chaining._
import scala.meta.internal.semanticdb.Range

import zugen.core.models.{Package, QualId, TemplateDefinitionName}

sealed trait ReferredSymbol {
  val startLine: Int
  val endLine: Int
  val startColumn: Int
  val endColumn: Int
  val symbol: String
  val pkg: Package = Package(symbol.split("/").toIndexedSeq.init.map(QualId))
}

object ReferredSymbol {

  private val MethodInvocationSuffix = "()."

  def of(symbol: String, range: Range): ReferredSymbol =
    symbol match {
      case s if s.endsWith(MethodInvocationSuffix) =>
        InvokedSymbol(
          startLine = range.startLine,
          endLine = range.endLine,
          startColumn = range.startCharacter,
          endColumn = range.endCharacter,
          symbol = symbol
        )
      case _ =>
        PlainSymbol(
          startLine = range.startLine,
          endLine = range.endLine,
          startColumn = range.startCharacter,
          endColumn = range.endCharacter,
          symbol = symbol
        )
    }

  final case class PlainSymbol(
    startLine: Int,
    endLine: Int,
    startColumn: Int,
    endColumn: Int,
    symbol: String
  ) extends ReferredSymbol

  final case class InvokedSymbol(
    startLine: Int,
    endLine: Int,
    startColumn: Int,
    endColumn: Int,
    symbol: String
  ) extends ReferredSymbol {

    val templateDefinitionName: TemplateDefinitionName =
      symbol.split("/").last.split("""[#\\.]""").head.pipe(TemplateDefinitionName)
  }
}

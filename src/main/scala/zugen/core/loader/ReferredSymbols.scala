package zugen.core.loader

import scala.meta.Position

/** Referred symbols occurred in source code */
final case class ReferredSymbols(elms: Seq[ReferredSymbol]) {

  def findByPosition(pos: Position): Option[ReferredSymbol] =
    elms.find { s =>
      s.startLine == pos.startLine &&
      s.endLine == pos.endLine &&
      s.startColumn == pos.startColumn &&
      s.endColumn == pos.endColumn
    }
}

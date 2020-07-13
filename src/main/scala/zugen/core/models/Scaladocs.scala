package zugen.core.models

/** Scaladocs in a source code file */
case class Scaladocs(blocks: Seq[Scaladoc]) {

  /** find scaladoc connected to given definition block */
  def findDocForDefinition(definition: Template): Option[Scaladoc] =
    blocks.find { block =>
      // 同じファイルに記述された、定義部の開始行の直前で終わるScaladocを、定義部に対するScaladocとみなす
      block.fileName == definition.fileName &&
      block.endLine == definition.startLine - 1
    }
}

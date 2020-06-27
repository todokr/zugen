package zugen.core.models

import zugen.core.models.Definitions.DefinitionBlock
import zugen.core.models.Scaladocs.ScaladocBlock

/**
  * 1ファイル分のscaladocの集合
  */
case class Scaladocs(blocks: Seq[ScaladocBlock]) {

  /**
    * 渡された定義部に紐づくScaladocを取得する
    */
  def findDocForDefinition(definition: DefinitionBlock): Option[ScaladocBlock] =
    blocks.find { block =>
      // 同じファイルに記述された、定義部の開始行の直前で終わるScaladocを、定義部に対するScaladocとみなす
      block.fileName == definition.fileName &&
      block.endLine == definition.startLine - 1
    }
}

object Scaladocs {

  /**
    * Scaladocのブロック
    */
  case class ScaladocBlock(fileName: FileName, startLine: Int, endLine: Int, content: String) {

    def firstLine: String = content.split("\n").head
  }
}

package tool.models

import tool.models.Definitions.DefinitionBlock
import tool.models.Scaladocs.ScaladocBlock

/**
  * 1ファイル分のscaladocの集合
  */
case class Scaladocs(blocks: Seq[ScaladocBlock]) {

  /**
    * 渡された定義部に紐づくScaladocを取得する
    */
  def findDocForDefinition(definition: DefinitionBlock): Option[ScaladocBlock] =
    blocks.find(_.endLine == definition.startLine - 1) // 定義部の開始行の直前で終わるScaladocを、定義部に対するScaladocとみなす
}

object Scaladocs {

  /**
    * Scaladocのブロック
    */
  case class ScaladocBlock(fileName: FileName, startLine: Int, endLine: Int, content: String)
}

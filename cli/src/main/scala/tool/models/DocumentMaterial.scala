package tool.models

import tool.models.Definitions.DefinitionBlock
import tool.models.DocumentMaterial.DocumentMaterialElement
import tool.models.Scaladocs.ScaladocBlock

/**
  * 生成されるドキュメントの大本となるデータ構造。クラスやトレイトの定義ブロックやScaladoc, 参照などを含む。
  */
case class DocumentMaterial(elms: Seq[DocumentMaterialElement])

object DocumentMaterial {

  /**
    * @param definition 定義ブロック
    * @param references 定義ブロック内に存在する、他の定義ブロックへの参照
    * @param scaladoc 定義ブロックに対応するScaladoc
    */
  case class DocumentMaterialElement(
    definition: DefinitionBlock,
    references: References,
    scaladoc: Option[ScaladocBlock]
  )
}

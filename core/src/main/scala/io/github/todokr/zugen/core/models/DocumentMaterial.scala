package io.github.todokr.zugen.core.models

import io.github.todokr.zugen.core.models.Definitions.DefinitionBlock
import io.github.todokr.zugen.core.models.DocumentMaterial.DocumentMaterialElement
import io.github.todokr.zugen.core.models.Scaladocs.ScaladocBlock

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

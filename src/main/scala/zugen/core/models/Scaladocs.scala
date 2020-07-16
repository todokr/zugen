package zugen.core.models

/** Scaladocs in a source code file */
final case class Scaladocs(elms: Seq[Scaladoc]) {

  /** find scaladoc connected to given definition block */
  def findDocForDefinition(definition: TemplateDefinition): Option[Scaladoc] =
    elms.find { block =>
      // 同じファイルに記述された、定義部の開始行の直前で終わるScaladocを、定義部に対するScaladocとみなす
      block.fileName == definition.fileName &&
      block.endLine == definition.startLine - 1
    }

  private val ScaladocLineNumOffset = -1

  def findByLineNum(declarationStartLineNum: Int): Option[Scaladoc] =
    elms.find { scaladoc =>
      scaladoc.endLine == declarationStartLineNum + ScaladocLineNumOffset
    }
}

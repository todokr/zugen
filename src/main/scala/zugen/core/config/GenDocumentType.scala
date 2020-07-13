package zugen.core.config

sealed trait GenDocumentType

object GenDocumentType {

  def from(code: String): Option[GenDocumentType] = {
    code match {
      case "domain-object-table"     => Some(GenDomainObjectTable)
      case "domain-relation-diagram" => Some(GenDomainRelationDiagram)
      case other =>
        println(s"${Console.YELLOW}[WARN]${Console.RESET} no such document type: $other")
        None
    }
  }
  val values: Seq[GenDocumentType] = Seq(GenDomainObjectTable, GenDomainRelationDiagram)

  /** table of domain objects */
  case object GenDomainObjectTable extends GenDocumentType

  /** relation diagram of domain objects */
  case object GenDomainRelationDiagram extends GenDocumentType
}

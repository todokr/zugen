package zugen.core.config

import scala.util.chaining._

sealed abstract class GenDocumentType(val code: String)

object GenDocumentType {

  def from(code: String): Option[GenDocumentType] =
    values.find(_.code == code).tap {
      case None => println(s"${Console.YELLOW}no such document type: $code${Console.RESET}")
      case _    =>
    }
  val values: Seq[GenDocumentType] =
    Seq(
      GenDomainObjectTable,
      GenDomainRelationDiagram,
      GenMethodInvocationDiagram
    )

  /** table of domain objects */
  case object GenDomainObjectTable extends GenDocumentType("domain-object-table")

  /** relation diagram of domain objects */
  case object GenDomainRelationDiagram extends GenDocumentType("domain-relation-diagram")

  /** method invocation diagram */
  case object GenMethodInvocationDiagram extends GenDocumentType("method-invocation-diagram")
}

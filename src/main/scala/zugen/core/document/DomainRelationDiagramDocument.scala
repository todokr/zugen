package zugen.core.document

import scala.util.chaining._

import zugen.core.config.Config
import zugen.core.document.DomainRelationDiagramDocument.Digraph
import zugen.core.models.Definitions.DefinitionBlock
import zugen.core.models.References.InternalReference
import zugen.core.models.References.InternalReference.{InternalInheritance, InternalProperty}
import zugen.core.models.Scaladocs.ScaladocBlock
import zugen.core.models.{DocumentMaterial, Package}

/** domain object relation diagram */
final case class DomainRelationDiagramDocument(digraph: Digraph) extends Document {

  override val docCode: String = "domain-relation-diagram"
  override val docName: String = "Domain Relation Diagram"
}

object DomainRelationDiagramDocument {

  def of(documentMaterial: DocumentMaterial, config: Config): DomainRelationDiagramDocument = {
    val domainPackages = config.domainPackages.map(n => Package(n.value))
    val domainInternalElements = documentMaterial.elms
      .filter(_.definition.isInAnyPackage(domainPackages))
      .filterNot(elm => config.domainObjectExcludePatterns.exists(p => elm.definition.name.value.matches(p)))

    val subGraphs = domainInternalElements
      .groupBy(_.definition.pkg)
      .map {
        case (pkg, materials) =>
          val nodes = materials.map(m => Node(m.definition, m.scaladoc))
          val subGraphId = SubGraph.genId(pkg)
          SubGraph(subGraphId, pkg.toString, nodes)
      }
      .toSeq

    val edges = domainInternalElements
      .flatMap { materialElm =>
        val definition = materialElm.definition
        materialElm.references.elms.collect {
          // relation between domain-internal nodes
          case ref: InternalReference if ref.definition.isInAnyPackage(domainPackages) =>
            val from = Node.genId(definition)
            val to = Node.genId(ref.definition)
            val arrowHead = ref match {
              case _: InternalInheritance => Normal
              case _: InternalProperty    => Diamond
            }
            DomainInternalEdge(from, to, arrowHead)
          // relation bounds to outside of domain
          case ref: InternalReference =>
            val from = Node.genId(definition)
            val toLabel = s"${ref.definition.pkg}.${ref.definition.name}"
            val toPkg = ref.definition.pkg
            val arrowHead = ref match {
              case _: InternalInheritance => Normal
              case _: InternalProperty    => Diamond
            }
            DomainExternalEdge(from, toLabel, toPkg, arrowHead)
        }
      }

    Digraph(
      label = "Domain object relations",
      subGraphs = subGraphs,
      edges = edges
    ).pipe(DomainRelationDiagramDocument(_))
  }

  case class Digraph(label: String, subGraphs: Seq[SubGraph], edges: Seq[Edge])
  case class SubGraph(id: SubGraphId, label: String, nodes: Seq[Node])
  sealed trait Edge

  /** edge of domain-internal */
  case class DomainInternalEdge(from: NodeId, to: NodeId, arrowHead: ArrowType) extends Edge

  /** edge bounds to outside of domain */
  case class DomainExternalEdge(from: NodeId, toLabel: String, toPkg: Package, arrowHead: ArrowType) extends Edge

  case class Node(id: NodeId, name: String, alias: Option[String]) {
    def label: String = alias.map(a => s"$name\n&quot;$a&quot;").getOrElse(name)
  }

  object SubGraph {

    def genId(pkg: Package): SubGraphId =
      SubGraphId(s"${pkg.elems.map(_.value).mkString("_")}")
  }

  object Node {

    def genId(definitionBlock: DefinitionBlock): NodeId =
      NodeId(s"${definitionBlock.pkg.elems.map(_.value).mkString("_")}_${definitionBlock.name.value}")

    def apply(definitionBlock: DefinitionBlock, scaladoc: Option[ScaladocBlock]): Node = {
      Node(
        id = genId(definitionBlock),
        name = definitionBlock.name.value,
        alias = scaladoc.map(_.firstLine) // 和名はScaladocの先頭行にあると仮定
      )
    }
  }

  case class SubGraphId(value: String) extends AnyVal {
    override def toString: String = value
  }

  case class NodeId(value: String) extends AnyVal {
    override def toString: String = value
  }

  sealed abstract class ArrowType(val code: String) {
    override def toString: String = code
  }
  case object Normal extends ArrowType("normal")
  case object Diamond extends ArrowType("odiamond")
}

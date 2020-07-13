package zugen.core.document

import scala.util.chaining._

import zugen.core.config.Config
import zugen.core.document.DomainRelationDiagramDocument.Digraph
import zugen.core.models.{DocumentMaterials, Packages, Scaladoc, Template}
import zugen.core.models.References.ProjectInternalReference.{ProjectInternalInheritance, ProjectInternalProperty}

/** domain object relation diagram */
final case class DomainRelationDiagramDocument(digraph: Digraph) extends Document {

  override val docCode: String = "domain-relation-diagram"
  override val docName: String = "Domain Relation Diagram"
}

object DomainRelationDiagramDocument {

  def of(documentMaterial: DocumentMaterials, config: Config): DomainRelationDiagramDocument = {
    val domainPackages = config.domainPackages.map(n => Packages(n.value))
    val domainInternalElements = documentMaterial.elms
      .filter(_.template.isInAnyPackage(domainPackages))
      .filterNot(elm => config.domainObjectExcludePatterns.exists(p => elm.template.name.value.matches(p)))

    val subGraphs = domainInternalElements
      .groupBy(_.template.pkg)
      .map {
        case (pkg, materials) =>
          val nodes = materials.map(m => Node(m.template, m.scaladoc))
          val subGraphId = SubGraph.genId(pkg)
          SubGraph(subGraphId, pkg.toString, nodes)
      }
      .toSeq

    val edges = domainInternalElements
      .flatMap { materialElm =>
        val template = materialElm.template
        materialElm.references.elms.collect {
          // domain-internal -> domain-internal
          case ref: ProjectInternalInheritance if ref.definition.isInAnyPackage(domainPackages) =>
            val from = Node.genId(template)
            val to = Node.genId(ref.definition)
            DomainInternalInheritanceEdge(from, to)
          // domain-internal -> domain-external
          case ref: ProjectInternalInheritance =>
            val from = Node.genId(template)
            val toLabel = s"${ref.definition.pkg}.${ref.definition.name}"
            DomainExternalInheritanceEdge(from, toLabel)
          // domain-internal -o domain-internal
          case ref: ProjectInternalProperty if ref.definition.isInAnyPackage(domainPackages) =>
            val from = Node.genId(template)
            val to = Node.genId(ref.definition)
            val propertyLabel = ref.memberName
            DomainInternalPropertyEdge(from, propertyLabel, to)
          // domain-internal -o domain-external
          case ref: ProjectInternalProperty =>
            val from = Node.genId(template)
            val propertyLabel = ref.memberName
            val toLabel = s"${ref.definition.pkg}.${ref.definition.name}"
            DomainExternalPropertyEdge(from, propertyLabel, toLabel)
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
  sealed trait Edge {
    def from: NodeId
  }

  /** edge of domain-internal inheritance */
  case class DomainInternalInheritanceEdge(from: NodeId, to: NodeId) extends Edge

  /** edge of inheritance of outside of domain */
  case class DomainExternalInheritanceEdge(from: NodeId, toLabel: String) extends Edge

  /** edge of domain-internal property */
  case class DomainInternalPropertyEdge(from: NodeId, propertyLabel: String, to: NodeId) extends Edge

  /** edge of property of outside of domain */
  case class DomainExternalPropertyEdge(from: NodeId, propertyLabel: String, toLabel: String) extends Edge

  case class Node(id: NodeId, name: String, alias: Option[String]) {
    def label: String = alias.map(a => s"$name\n&quot;$a&quot;").getOrElse(name)
  }

  object SubGraph {

    def genId(pkg: Packages): SubGraphId =
      SubGraphId(s"${pkg.elms.map(_.value).mkString("_")}")
  }

  object Node {

    def genId(definitionBlock: Template): NodeId =
      NodeId(s"${definitionBlock.pkg.elms.map(_.value).mkString("_")}_${definitionBlock.name.value}")

    def apply(definitionBlock: Template, scaladoc: Option[Scaladoc]): Node = {
      Node(
        id = genId(definitionBlock),
        name = definitionBlock.name.value,
        alias = scaladoc.map(_.firstLine) // alias should be in head of lines
      )
    }
  }

  case class SubGraphId(value: String) extends AnyVal {
    override def toString: String = value
  }

  case class NodeId(value: String) extends AnyVal {
    override def toString: String = value
  }
}

package zugen.core.document

import scala.util.chaining._

import zugen.core.config.Config
import zugen.core.document.DomainRelationDiagramDocument.Digraph
import zugen.core.models.References.ProjectInternalReference.{ProjectInternalInheritance, ProjectInternalProperty}
import zugen.core.models.{DocumentMaterials, Package, TemplateDefinition}

/** domain object relation diagram */
final case class DomainRelationDiagramDocument(digraph: Digraph) extends Document {

  override val docCode: String = "domain-relation-diagram"
  override val docName: String = "Domain Relation Diagram"
}

object DomainRelationDiagramDocument {

  def of(documentMaterial: DocumentMaterials, config: Config): DomainRelationDiagramDocument = {
    val domainPackages = config.domainPackages.map(n => Package(n.value))
    val domainInternalElements = documentMaterial.elms
      .filter(_.templateDefinition.isInAnyPackage(domainPackages))
      .filterNot(elm => config.domainObjectExcludePatterns.exists(p => elm.templateDefinition.name.value.matches(p)))

    val edges = domainInternalElements
      .flatMap { materialElm =>
        val template = materialElm.templateDefinition
        materialElm.references.elms.collect {
          // domain-internal -> domain-internal
          case ref: ProjectInternalInheritance if ref.definition.isInAnyPackage(domainPackages) =>
            val from = Node.of(template, config)
            val to = Node.of(ref.definition, config)
            DomainInternalInheritanceEdge(from, to)
          // domain-internal -> domain-external
          case ref: ProjectInternalInheritance =>
            val from = Node.of(template, config)
            val to = Node.of(ref.definition, config)
            DomainExternalInheritanceEdge(from, to)
          // domain-internal -o domain-internal
          case ref: ProjectInternalProperty if ref.definition.isInAnyPackage(domainPackages) =>
            val from = Node.of(template, config)
            val to = Node.of(ref.definition, config)
            val propertyLabel = ref.memberName
            DomainInternalPropertyEdge(from, propertyLabel, to)
          // domain-internal -o domain-external
          case ref: ProjectInternalProperty =>
            val from = Node.of(template, config)
            val propertyLabel = ref.memberName
            val to = Node.of(ref.definition, config)
            DomainExternalPropertyEdge(from, propertyLabel, to)
        }
      }

    val nodes = edges.flatMap(edge => Seq(edge.to, edge.from))
    val subGraphs = nodes.groupBy(_.pkg).map {
      case (pkg, nodes) =>
        val subGraphId = SubGraph.genId(pkg)
        SubGraph(subGraphId, pkg.toString, nodes)
    }.toSeq

    Digraph(
      label = "Domain object relations",
      subGraphs = subGraphs,
      edges = edges
    ).pipe(DomainRelationDiagramDocument(_))
  }

  final case class Digraph(label: String, subGraphs: Seq[SubGraph], edges: Seq[Edge])
  final case class SubGraph(id: SubGraphId, label: String, nodes: Seq[Node])
  sealed trait Edge {
    def from: Node
    def to: Node
  }

  /** edge of domain-internal inheritance */
  final case class DomainInternalInheritanceEdge(from: Node, to: Node) extends Edge

  /** edge of inheritance of outside of domain */
  final case class DomainExternalInheritanceEdge(from: Node, to: Node) extends Edge

  /** edge of domain-internal property */
  final case class DomainInternalPropertyEdge(from: Node, propertyLabel: String, to: Node) extends Edge

  /** edge of property of outside of domain */
  final case class DomainExternalPropertyEdge(from: Node, propertyLabel: String, to: Node) extends Edge

  final case class Node(
    id: NodeId,
    pkg: Package,
    name: String,
    alias: Option[String],
    fileUrl: Option[String]
  ) {
    def label: String = alias.map(a => s"$name\n&quot;$a&quot;").getOrElse(name)
  }

  object SubGraph {

    def genId(pkg: Package): SubGraphId =
      SubGraphId(s"${pkg.ids.map(_.value).mkString("_")}")
  }

  object Node {

    def genId(templateDefinition: TemplateDefinition): NodeId =
      NodeId(s"${templateDefinition.pkg.ids.map(_.value).mkString("_")}_${templateDefinition.name.value}")

    def of(templateDefinition: TemplateDefinition, config: Config): Node = {
      Node(
        id = genId(templateDefinition),
        pkg = templateDefinition.pkg,
        name = templateDefinition.name.value,
        alias = templateDefinition.scaladoc.map(_.firstLine), // alias should be in head of lines
        fileUrl = config.githubBaseUrl.map(baseUrl => s"$baseUrl/${templateDefinition.fileName}")
      )
    }
  }

  final case class SubGraphId(value: String) extends AnyVal {
    override def toString: String = value
  }

  final case class NodeId(value: String) extends AnyVal {
    override def toString: String = value
  }
}

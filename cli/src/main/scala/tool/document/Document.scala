package tool.document

import scala.util.chaining._

import tool.Config
import tool.document.Document.DomainObjectTableDoc.DomainObjectTableRow
import tool.document.Document.DomainRelationDiagramDoc.Digraph
import tool.models.Definitions.DefinitionBlock
import tool.models.References.InternalReference
import tool.models.References.InternalReference.{InternalInheritance, InternalProperty}
import tool.models.Scaladocs.ScaladocBlock
import tool.models.{DefinitionName, DocumentMaterial, FileName, Package}

/**
  * 生成されるドキュメントのデータ
  */
sealed trait Document {

  val docName: String
}

object Document {

  /**
    * ドメインオブジェクトの表
    */
  final case class DomainObjectTableDoc(rows: Seq[DomainObjectTableRow]) extends Document {

    override val docName: String = "domain-object-table"
  }

  object DomainObjectTableDoc {

    def of(documentMaterial: DocumentMaterial): DomainObjectTableDoc =
      documentMaterial.elms.map { elm =>
        DomainObjectTableRow(
          pkg = elm.definition.pkg,
          name = elm.definition.name,
          scaladoc = elm.scaladoc.map(_.content).getOrElse("-"),
          fileName = elm.definition.fileName
        )
      }.pipe(DomainObjectTableDoc(_))

    final case class DomainObjectTableRow(
      pkg: Package,
      name: DefinitionName,
      scaladoc: String,
      fileName: FileName
    )
  }

  /**
    * ドメインのパッケージ関連図
    */
  final case class DomainRelationDiagramDoc(digraph: Digraph) extends Document {

    override val docName: String = "domain-relation-diagram"
  }

  object DomainRelationDiagramDoc {

    /**
      * ドキュメントの生成元データからドメイン関連図データを組み立てる。
      */
    def of(documentMaterial: DocumentMaterial, config: Config): DomainRelationDiagramDoc = {
      val domainPackages = config.domainPackages.map(n => Package(n.value))

      val subGraphs = documentMaterial.elms
        .filter(elm => elm.definition.isInAnyPackage(domainPackages)) // サブグラフとノードは、指定されたパッケージに所属する定義ブロックのみに絞り込む
        .groupBy(_.definition.pkg)
        .map {
          case (pkg, materials) =>
            val nodes = materials.map(m => Node(m.definition, m.scaladoc))
            val subGraphId = SubGraph.genId(pkg)
            SubGraph(subGraphId, pkg.toString, nodes)
        }
        .toSeq

      val edges = documentMaterial.elms
        .flatMap { materialElm =>
          val definition = materialElm.definition
          materialElm.references.elms.collect {
            case ref: InternalReference if ref.definition.isInAnyPackage(domainPackages) =>
              val from = Node.genId(definition)
              val to = Node.genId(ref.definition)
              val arrowHead = ref match {
                case _: InternalInheritance => Normal
                case _: InternalProperty    => Diamond
              }
              DomainInternalEdge(from, to, arrowHead)
            case ref: InternalReference =>
              val from = Node.genId(definition)
              val toLabel = s"${ref.definition.pkg}\n${ref.definition.name}"
              val toPkg = ref.definition.pkg
              val arrowHead = ref match {
                case _: InternalInheritance => Normal
                case _: InternalProperty    => Diamond
              }
              DomainExternalEdge(from, toLabel, toPkg, arrowHead)
          }
        }

      Digraph(
        label = "Domain model relations",
        subGraphs = subGraphs,
        edges = edges
      ).pipe(DomainRelationDiagramDoc(_))
    }

    case class Digraph(label: String, subGraphs: Seq[SubGraph], edges: Seq[Edge])
    case class SubGraph(id: SubGraphId, label: String, nodes: Seq[Node])
    sealed trait Edge

    /**
      * ドメインパッケージ内のノード間のエッジ
      */
    case class DomainInternalEdge(from: NodeId, to: NodeId, arrowHead: ArrowType) extends Edge

    /**
      * ドメインパッケージ外のノードへのエッジ
      */
    case class DomainExternalEdge(from: NodeId, toLabel: String, toPkg: Package, arrowHead: ArrowType) extends Edge

    case class Node(id: NodeId, name: String, alias: Option[String]) {
      def label: String = alias.map(a => s"$name\n$a").getOrElse(name)
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
}

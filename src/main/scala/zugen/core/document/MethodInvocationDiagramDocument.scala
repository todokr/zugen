package zugen.core.document

import zugen.core.config.Config
import zugen.core.document.MethodInvocationDiagramDocument.Node.{ExternalNode, InternalNode}
import zugen.core.document.MethodInvocationDiagramDocument.{Edge, Node}
import zugen.core.models.{DocumentMaterials, InvokeTarget, Method, MethodName, Package, TemplateDefinitionName}

final case class MethodInvocationDiagramDocument(nodes: Seq[Node], edges: Seq[Edge]) extends Document {
  override val docCode: String = "method-invocation-diagram"
  override val docName: String = "Method Invocation Diagram"
}

object MethodInvocationDiagramDocument {

  def of(documentMaterial: DocumentMaterials, config: Config): MethodInvocationDiagramDocument = {
    config.methodInvocationStartingPackage match {
      case Some(p) =>
        val startingPackage = Package(p.value)
        val allMethods = documentMaterial.elms.flatMap { material =>
          material.templateDefinition.methods
        }
        val startingMethods = allMethods.filter(_.pkg == startingPackage)
        val invocations = startingMethods.flatMap(buildInvocations(_, allMethods))

        val nodes: Seq[Node] = invocations.flatMap(i => Seq(i.from, i.to)).toSet
          .map { item: InvocationItem =>
            val material = documentMaterial.elms.find(elm =>
              elm.templateDefinition.pkg == item.pkg && elm.templateDefinition.name == item.templateDefinitionName)
            item -> material
          }.collect {
            case (item, Some(material)) =>
              InternalNode(
                id = NodeId(item.itemId),
                pkg = item.pkg,
                name = s"${item.templateDefinitionName}#${item.methodName}",
                fileUrl =
                  config.githubBaseUrl.map(baseUrl => s"$baseUrl/${material.templateDefinition.fileName.value}"),
                isTopLevel = item.isTopLevel
              )
            case (item, _) =>
              ExternalNode(
                id = NodeId(item.itemId),
                pkg = item.pkg,
                name = s"${item.templateDefinitionName}#${item.methodName}"
              )
          }.toSeq
        val edges = invocations.map(i => Edge(NodeId(i.from.itemId), NodeId(i.to.itemId)))
        MethodInvocationDiagramDocument(nodes, edges)
      case None =>
        println(s"${Console.YELLOW}[WARN]${Console.RESET} Config for Method Invocation Diagram not found.")
        MethodInvocationDiagramDocument(Seq.empty, Seq.empty)
    }
  }
  final case class Invocation(from: InvocationItem, to: InvocationItem) {
    override def toString: String = s"$from --> $to"
  }
  final case class InvocationItem(
    itemId: String,
    pkg: Package,
    templateDefinitionName: TemplateDefinitionName,
    methodName: MethodName,
    isTopLevel: Boolean
  ) {
    override def toString: String = s"$pkg.$templateDefinitionName#$methodName"
  }

  val LimitInvocationLevel = 100

  /** traverse method invocations and build invocation collection */
  def buildInvocations(start: Method, allMethods: Seq[Method]): Seq[Invocation] = {
    def _loop(invoker: Method, allMethods: Seq[Method], level: Int): Seq[Invocation] = {
      if (level >= LimitInvocationLevel)
        throw new TooDeepMethodInvocationException(s"limit exceeded. method=${invoker}, limit=$LimitInvocationLevel")
      invoker.invokeTargets match {
        case targets if targets.isEmpty => Seq.empty
        case targets =>
          val from = InvocationItem(
            itemId = genItemId(invoker),
            pkg = invoker.pkg,
            templateDefinitionName = invoker.templateDefinitionName,
            methodName = invoker.methodName,
            isTopLevel = level == 0
          )
          val currentLevelInvocations = targets.map { next =>
            val to = InvocationItem(
              itemId = genItemId(next),
              pkg = next.pkg,
              templateDefinitionName = next.templateDefinitionName,
              methodName = next.methodName,
              isTopLevel = false
            )
            Invocation(from, to)
          }
          val nextMethods = targets.flatMap { invoke =>
            allMethods.filter { m =>
              m.pkg == invoke.pkg &&
              m.templateDefinitionName == invoke.templateDefinitionName &&
              m.methodName == invoke.methodName
            }
          }
          currentLevelInvocations ++ nextMethods.flatMap(next => _loop(next, allMethods, level + 1))
      }
    }
    _loop(start, allMethods, 0)
  }

  def genItemId(method: Method) =
    s"${method.pkg.toString.replace(".", "_")}_${method.templateDefinitionName}_${method.methodName}"

  def genItemId(target: InvokeTarget) =
    s"${target.pkg.toString.replace(".", "_")}_${target.templateDefinitionName}_${target.methodName}"

  class TooDeepMethodInvocationException(msg: String) extends Exception(msg)

  sealed trait Node {
    def id: NodeId
    def pkg: Package
    def name: String
    def isTopLevel: Boolean
  }
  object Node {
    final case class InternalNode(
      id: NodeId,
      pkg: Package,
      name: String,
      fileUrl: Option[String],
      isTopLevel: Boolean
    ) extends Node

    final case class ExternalNode(
      id: NodeId,
      pkg: Package,
      name: String
    ) extends Node {
      override val isTopLevel = false
    }
  }

  final case class Edge(from: NodeId, to: NodeId)
  final case class NodeId(value: String) extends AnyVal {
    override def toString: String = value
  }
}

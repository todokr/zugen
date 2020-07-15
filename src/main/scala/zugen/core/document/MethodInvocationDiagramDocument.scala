package zugen.core.document

import zugen.core.config.Config
import zugen.core.document.MethodInvocationDiagramDocument.Invocation
import zugen.core.models.{DocumentMaterials, InvokeTarget, Method, MethodName, Package, TemplateDefinitionName}

case class MethodInvocationDiagramDocument(invocations: Seq[Invocation]) extends Document {
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
        val (startingMethods, restMethods) = allMethods.partition(_.pkg == startingPackage)
        val invocations = (startingMethods ++ restMethods).flatMap { method =>
          val from = InvocationItem(genItemId(method), method.pkg, method.templateDefinitionName, method.methodName)
          method.invokeTargets.map { target =>
            val to = InvocationItem(genItemId(target), target.pkg, target.templateDefinitionName, target.methodName)
            Invocation(from, to)
          }
        }
        MethodInvocationDiagramDocument(invocations)
      case None =>
        println(s"${Console.YELLOW}[WARN]${Console.RESET} Config for Method Invocation Diagram not found.")
        MethodInvocationDiagramDocument(Seq.empty)
    }
  }

  def genItemId(method: Method) =
    s"${method.pkg.toString.replace(".", "_")}_${method.templateDefinitionName}_${method.methodName}"

  def genItemId(target: InvokeTarget) =
    s"${target.pkg.toString.replace(".", "_")}_${target.templateDefinitionName}_${target.methodName}"

  final case class Invocation(from: InvocationItem, to: InvocationItem)
  final case class InvocationItem(
    itemId: String,
    pkg: Package,
    templateDefinitionName: TemplateDefinitionName,
    methodName: MethodName
  )

  sealed trait InvocationTree

  object InvocationTree {

    val LimitInvocationLevel = 100
    final case class InvocationLeaf(
      pkg: Package,
      templateDefinitionName: TemplateDefinitionName,
      methodName: MethodName,
      level: Int
    ) extends InvocationTree
    final case class InvocationBranch(
      pkg: Package,
      templateDefinitionName: TemplateDefinitionName,
      methodName: MethodName,
      level: Int,
      targets: Seq[InvocationTree]
    ) extends InvocationTree

    def construct(start: Method, allMethods: Seq[Method]): InvocationTree = {
      // TODO tailrec
      def _loop(invoker: Method, allMethods: List[Method], level: Int): InvocationTree = {
        if (level >= LimitInvocationLevel)
          throw new TooDeepMethodInvocationException(s"limit exceeded. method=${invoker}, limit=$LimitInvocationLevel")
        invoker.invokeTargets match {
          case targets if targets.isEmpty =>
            InvocationLeaf(
              pkg = invoker.pkg,
              templateDefinitionName = invoker.templateDefinitionName,
              methodName = invoker.methodName,
              level = level
            )
          case invokes =>
            val nextMethods = invokes.flatMap { invoke =>
              allMethods.filter { m =>
                m.pkg == invoke.pkg &&
                m.templateDefinitionName == invoke.templateDefinitionName &&
                m.methodName == invoke.methodName
              }
            }
            InvocationBranch(
              pkg = invoker.pkg,
              templateDefinitionName = invoker.templateDefinitionName,
              methodName = invoker.methodName,
              level = level,
              targets = nextMethods.map(m => _loop(m, allMethods, level + 1))
            )
        }
      }
      _loop(start, allMethods.toList, 0)
    }

    class TooDeepMethodInvocationException(msg: String) extends Exception(msg)
  }

}

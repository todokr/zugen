package zugen.core.document

import zugen.core.config.Config
import zugen.core.document.MethodInvocationDiagramDocument.InvocationTree
import zugen.core.models.{DocumentMaterials, Method, MethodName, Package, TemplateDefinitionName}

case class MethodInvocationDiagramDocument(invocationTrees: Seq[InvocationTree]) extends Document {
  override val docCode: String = "method-invocation-diagram"
  override val docName: String = "Method Invocation Diagram"
}

object MethodInvocationDiagramDocument {

  def of(documentMaterial: DocumentMaterials, config: Config): MethodInvocationDiagramDocument = {
    config.methodInvocationStartingPackage match {
      case Some(p) =>
        val startingPackage = Package(p.value)
        val startingMethods = documentMaterial.elms.collect {
          case material if material.templateDefinition.pkg == startingPackage =>
            material.templateDefinition.methods
        }.flatten
        startingMethods.foreach(println)
        MethodInvocationDiagramDocument(Seq.empty)
      case None =>
        println(s"${Console.YELLOW}[WARN]${Console.RESET} Config for Method Invocation Diagram not found.")
        MethodInvocationDiagramDocument(Seq.empty)
    }
  }

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

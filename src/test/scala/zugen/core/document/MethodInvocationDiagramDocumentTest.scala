package zugen.core.document

import org.scalatest.funsuite.AnyFunSuite
import zugen.core.document.MethodInvocationDiagramDocument.{genItemId, Invocation, InvocationItem}
import zugen.core.models.{InvokeTarget, Method, MethodName, Package, QualId, TemplateDefinitionName}

class MethodInvocationDiagramDocumentTest extends AnyFunSuite {

  test("buildInvocations") {
    def method(x: String, invokes: Seq[String]): Method =
      Method(
        pkg = Package(Seq(QualId(s"package${x.toLowerCase}"))),
        templateDefinitionName = TemplateDefinitionName(s"Class$x"),
        methodName = MethodName(x),
        invokeTargets = invokes.map { i =>
          InvokeTarget(
            pkg = Package(Seq(QualId(s"package${i.toLowerCase}"))),
            templateDefinitionName = TemplateDefinitionName(s"Class$i"),
            methodName = MethodName(i))
        }
      )
    val X = method("X", Seq("A"))
    val A = method("A", Seq("B", "D", "J"))
    val B = method("B", Seq("C", "G"))
    val C = method("C", Seq())
    val D = method("D", Seq("E"))
    val E = method("E", Seq("F"))
    val F = method("F", Seq())
    val G = method("G", Seq("H"))
    val H = method("H", Seq("I"))
    val I = method("I", Seq())
    val J = method("J", Seq("B"))
    val K = method("K", Seq("L", "M", "N"))
    val L = method("L", Seq.empty)
    val M = method("M", Seq.empty)
    val N = method("N", Seq.empty)
    val allMethods = Seq(X, A, B, C, D, E, F, G, H, I, J, K, L, M, N)
    val startingPackage = A.pkg

    implicit class RichMethod(underlying: Method) {
      def -->(target: Method): Invocation = {
        val from = InvocationItem(
          itemId = genItemId(underlying),
          pkg = underlying.pkg,
          templateDefinitionName = underlying.templateDefinitionName,
          methodName = underlying.methodName,
          isTopLevel = underlying.pkg == startingPackage
        )
        val to = InvocationItem(
          itemId = genItemId(target),
          pkg = target.pkg,
          templateDefinitionName = target.templateDefinitionName,
          methodName = target.methodName,
          isTopLevel = target.pkg == startingPackage
        )
        Invocation(from, to)
      }
    }

    val actual = MethodInvocationDiagramDocument.buildInvocations(A, allMethods)
    val expected = Seq(
      A --> B,
      A --> D,
      A --> J,
      B --> C,
      B --> G,
      G --> H,
      H --> I,
      D --> E,
      E --> F,
      J --> B,
      B --> C,
      B --> G,
      G --> H,
      H --> I
    )

    assert(actual == expected)
  }
}

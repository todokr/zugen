package zugen.core.document

import org.scalatest.funsuite.AnyFunSuite
import zugen.core.document.MethodInvocationDiagramDocument.InvocationTree
import zugen.core.document.MethodInvocationDiagramDocument.InvocationTree.{
  InvocationBranch,
  InvocationLeaf,
  TooDeepMethodInvocationException
}
import zugen.core.models.{InvokeTarget, Method, MethodName, Package, QualId, TemplateDefinitionName}

class InvocationTreeTest extends AnyFunSuite {

  test("construct invocation tree") {

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
    val allMethods = Seq(A, B, C, D, E, F, G, H, I, J)

    val actual = InvocationTree.construct(A, allMethods)
    val expected = InvocationBranch(
      pkg = A.pkg,
      templateDefinitionName = A.templateDefinitionName,
      methodName = A.methodName,
      level = 0,
      targets = Seq(
        InvocationBranch(
          pkg = B.pkg,
          templateDefinitionName = B.templateDefinitionName,
          methodName = B.methodName,
          level = 1,
          targets = Seq(
            InvocationLeaf(
              pkg = C.pkg,
              templateDefinitionName = C.templateDefinitionName,
              methodName = C.methodName,
              level = 2),
            InvocationBranch(
              pkg = G.pkg,
              templateDefinitionName = G.templateDefinitionName,
              methodName = G.methodName,
              level = 2,
              targets = Seq(
                InvocationBranch(
                  pkg = H.pkg,
                  templateDefinitionName = H.templateDefinitionName,
                  methodName = H.methodName,
                  level = 3,
                  targets = Seq(
                    InvocationLeaf(
                      pkg = I.pkg,
                      templateDefinitionName = I.templateDefinitionName,
                      methodName = I.methodName,
                      level = 4))
                ))
            )
          )
        ),
        InvocationBranch(
          pkg = D.pkg,
          templateDefinitionName = D.templateDefinitionName,
          methodName = D.methodName,
          level = 1,
          targets = Seq(
            InvocationBranch(
              pkg = E.pkg,
              templateDefinitionName = E.templateDefinitionName,
              methodName = E.methodName,
              level = 2,
              targets = Seq(
                InvocationLeaf(
                  pkg = F.pkg,
                  templateDefinitionName = F.templateDefinitionName,
                  methodName = F.methodName,
                  level = 3))
            ))
        ),
        InvocationBranch(
          pkg = J.pkg,
          templateDefinitionName = J.templateDefinitionName,
          methodName = J.methodName,
          level = 1,
          targets = Seq(
            InvocationBranch(
              pkg = B.pkg,
              templateDefinitionName = B.templateDefinitionName,
              methodName = B.methodName,
              level = 2,
              targets = Seq(
                InvocationLeaf(
                  pkg = C.pkg,
                  templateDefinitionName = C.templateDefinitionName,
                  methodName = C.methodName,
                  level = 3
                ),
                InvocationBranch(
                  pkg = G.pkg,
                  templateDefinitionName = G.templateDefinitionName,
                  methodName = G.methodName,
                  level = 3,
                  targets = Seq(
                    InvocationBranch(
                      pkg = H.pkg,
                      templateDefinitionName = H.templateDefinitionName,
                      methodName = H.methodName,
                      level = 4,
                      targets = Seq(
                        InvocationLeaf(
                          pkg = I.pkg,
                          templateDefinitionName = I.templateDefinitionName,
                          methodName = I.methodName,
                          level = 5))
                    ))
                )
              )
            ))
        )
      )
    )

    assert(actual == expected)
  }

  test("limit exceed") {
    val infiniteMethod = Method(
      Package(Seq(QualId("a"))),
      TemplateDefinitionName("A"),
      MethodName("a()"),
      invokeTargets = Seq(
        InvokeTarget(
          Package(Seq(QualId("a"))),
          TemplateDefinitionName("A"),
          MethodName("a()")
        )
      )
    )
    assertThrows[TooDeepMethodInvocationException](InvocationTree.construct(infiniteMethod, Seq(infiniteMethod)))
  }
}

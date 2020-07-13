package zugen.core.document

import org.scalatest.funsuite.AnyFunSuite
import zugen.core.document.InvocationTree.{InvocationBranch, InvocationLeaf, TooDeepMethodInvocationException}
import zugen.core.models.{Method, MethodInvocation, MethodName, Package, QualId, TemplateName}

class InvocationTreeTest extends AnyFunSuite {

  test("construct invocation tree") {

    def method(x: String): Method =
      Method(
        pkg = Package(Seq(QualId(s"package${x.toLowerCase}"))),
        templateName = TemplateName(s"Class$x"),
        methodName = MethodName(x)
      )

    val A = method("A")
    val B = method("B")
    val C = method("C")
    val D = method("D")
    val E = method("E")
    val F = method("F")
    val G = method("G")
    val H = method("H")
    val I = method("I")
    val J = method("J")

    val invocations = Seq(
      A -> B,
      B -> C,
      B -> G,
      G -> H,
      H -> I,
      A -> D,
      D -> E,
      E -> F,
      A -> J,
      J -> B
    ).map {
      case (from, to) => MethodInvocation(invoker = from, target = to)
    }

    val actual = InvocationTree.construct(A, invocations)
    val expected = InvocationBranch(
      invoker = A,
      level = 0,
      invokes = Seq(
        InvocationBranch(
          invoker = B,
          level = 1,
          invokes = Seq(
            InvocationLeaf(C, 2),
            InvocationBranch(
              invoker = G,
              level = 2,
              invokes = Seq(
                InvocationBranch(
                  invoker = H,
                  level = 3,
                  invokes = Seq(InvocationLeaf(I, 4))))))
        ),
        InvocationBranch(
          invoker = D,
          level = 1,
          invokes = Seq(
            InvocationBranch(
              invoker = E,
              level = 2,
              invokes = Seq(InvocationLeaf(F, 3))))),
        InvocationBranch(
          invoker = J,
          level = 1,
          invokes = Seq(
            InvocationBranch(
              invoker = B,
              level = 2,
              invokes = Seq(
                InvocationLeaf(C, 3),
                InvocationBranch(
                  invoker = G,
                  level = 3,
                  invokes = Seq(
                    InvocationBranch(
                      invoker = H,
                      level = 4,
                      invokes = Seq(InvocationLeaf(I, 5))))))
            ))
        )
      )
    )

    assert(actual == expected)
  }

  test("limit exceed") {
    val invocations = Iterator.from(1).sliding(2).map {
      case Seq(a, b) =>
        val methodA = Method(Package(Seq(QualId(s"$a"))), TemplateName(s"$a"), MethodName(s"$a"))
        val methodB = Method(Package(Seq(QualId(s"$b"))), TemplateName(s"$b"), MethodName(s"$b"))
        MethodInvocation(methodA, methodB)
    }.take(200).toSeq

    val start = invocations.head.invoker
    assertThrows[TooDeepMethodInvocationException](InvocationTree.construct(start, invocations))
  }
}

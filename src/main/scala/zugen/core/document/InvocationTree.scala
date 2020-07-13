package zugen.core.document

import zugen.core.models.{Method, MethodInvocation}

sealed trait InvocationTree

object InvocationTree {

  val LimitInvocationLevel = 100
  final case class InvocationLeaf(method: Method, level: Int) extends InvocationTree
  final case class InvocationBranch(invoker: Method, level: Int, invokes: Seq[InvocationTree]) extends InvocationTree

  def construct(start: Method, invocations: Seq[MethodInvocation]): InvocationTree = {
    // TODO tailrec
    def _loop(invoker: Method, invocations: List[MethodInvocation], level: Int): InvocationTree = {
      if (level >= LimitInvocationLevel)
        throw new TooDeepMethodInvocationException(s"limit exceeded. method=${invoker}, limit=$LimitInvocationLevel")
      invocations.filter(_.invoker == invoker) match {
        case Nil     => InvocationLeaf(invoker, level)
        case invokes => InvocationBranch(invoker, level, invokes.map(i => _loop(i.target, invocations, level + 1)))
      }
    }
    _loop(start, invocations.toList, 0)
  }

  class TooDeepMethodInvocationException(msg: String) extends Exception(msg)
}

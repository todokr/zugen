package zugen.core.models

final case class MethodInvocation(
  invoker: Method,
  target: Method
)

final case class MethodInvocations(elms: Seq[MethodInvocation])

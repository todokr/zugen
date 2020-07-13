package zugen.core.models

final case class MethodInvocation(
  invoker: Method,
  target: Method
)

final case class Method(
  pkg: Package,
  templateName: TemplateName,
  methodName: MethodName
)

case class MethodName(value: String) extends AnyVal

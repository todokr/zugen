package zugen.core.models

final case class Method(
  pkg: Package,
  templateDefinitionName: TemplateDefinitionName,
  methodName: MethodName,
  invokeTargets: Seq[InvokeTarget] // invocations of other methods in this method
) {

  override def toString: String =
    if (invokeTargets.nonEmpty) {
      s"${pkg}/$templateDefinitionName#$methodName invokes\n" + invokeTargets.map { invoke =>
        s"  -> ${invoke.pkg}/${invoke.templateDefinitionName}#${invoke.methodName}"
      }.mkString("\n")
    } else {
      s"${pkg}/$templateDefinitionName#$methodName invokes\n  <nothing>"
    }

}

final case class InvokeTarget(
  pkg: Package,
  templateDefinitionName: TemplateDefinitionName,
  methodName: MethodName
)

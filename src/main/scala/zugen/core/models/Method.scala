package zugen.core.models

final case class Method(
  pkg: Package,
  templateDefinitionName: TemplateDefinitionName,
  methodName: MethodName,
  invokes: Seq[Invoke] // invocations of other methods in this method
) {

  override def toString: String =
    if (invokes.nonEmpty) {
      s"${pkg}/$templateDefinitionName#$methodName invokes\n" + invokes.map { invoke =>
        s"  -> ${invoke.pkg}/${invoke.templateDefinitionName}#${invoke.methodName}"
      }.mkString("\n")
    } else {
      s"${pkg}/$templateDefinitionName#$methodName invokes\n  <nothing>"
    }

}

final case class Invoke(
  pkg: Package,
  templateDefinitionName: TemplateDefinitionName,
  methodName: MethodName
)

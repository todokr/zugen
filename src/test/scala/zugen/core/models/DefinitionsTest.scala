package zugen.core.models

import org.scalatest.funsuite.AnyFunSuite
import zugen.core.models.TemplateDefinition.TraitDefinition

class DefinitionsTest extends AnyFunSuite {

  test("isInAnyPackages") {

    val block = TraitDefinition(
      pkg = Package("example.domain.model"),
      name = TemplateDefinitionName("SomeTrait"),
      modifier = Modifiers(Seq.empty),
      parents = Parents(Seq.empty),
      methods = Seq.empty,
      scaladoc = None,
      fileName = FileName("SomeTrait"),
      startLine = 0,
      endLine = 10
    )

    val targetPackages = Seq("example.domain", "example.domain2").map(Package)

    assert(block.isInAnyPackage(targetPackages))
  }
}

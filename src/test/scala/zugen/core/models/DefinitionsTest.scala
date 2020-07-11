package zugen.core.models

import org.scalatest.funsuite.AnyFunSuite
import zugen.core.models.Definitions.DefinitionBlock.TraitDefinitionBlock

class DefinitionsTest extends AnyFunSuite {

  test("isInAnyPackages") {

    val block = TraitDefinitionBlock(
      name = DefinitionName("SomeTrait"),
      modifier = Modifiers(Seq.empty),
      parents = Parents(Seq.empty),
      pkg = Package("example.domain.model"),
      fileName = FileName("SomeTrait"),
      startLine = 0,
      endLine = 10
    )

    val targetPackages = Seq("example.domain", "example.domain2").map(Package)

    assert(block.isInAnyPackage(targetPackages))
  }
}

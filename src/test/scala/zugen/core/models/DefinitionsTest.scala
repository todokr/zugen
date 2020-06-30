package zugen.core.models

import scala.util.chaining._

import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import Definitions.DefinitionBlock.ClassDefinitionBlock
import Scaladocs.ScaladocBlock

class DefinitionsTest extends AnyWordSpec with Matchers with OptionValues {

  "findDocForDefinition" can {

    "given definition block corresponds to scaladoc" should {
      "return the scaladoc block" in {
        val fileNameOfClass = FileName("example/somepackage/SomeClass.scala")
        val classContent = "An awesome class"
        val scaladocBlock =
          ScaladocBlock(
            fileName = fileNameOfClass,
            startLine = 0,
            endLine = 2,
            content = classContent
          )
        val scaladocs =
          Seq(scaladocBlock).pipe(Scaladocs(_))

        val classDefinition =
          ClassDefinitionBlock(
            name = DefinitionName("SomeClass"),
            modifier = Modifiers(Seq.empty),
            parents = Parents(Seq.empty),
            pkg = Package("example.somepackage"),
            constructor = Constructor(Seq.empty),
            fileName = fileNameOfClass,
            startLine = 3,
            endLine = 10
          )

        val actual = scaladocs.findDocForDefinition(classDefinition)

        actual.value shouldBe scaladocBlock
      }
    }

    "there is no scaladoc corresponds to given definition" should {
      "return None" in {
        val fileNameOfClass = FileName("example/somepackage/SomeClass.scala")
        val classContent = "An awesome class"
        val scaladocBlock =
          ScaladocBlock(
            fileName = fileNameOfClass,
            startLine = 100,
            endLine = 101,
            content = classContent
          )
        val scaladocs =
          Seq(scaladocBlock).pipe(Scaladocs(_))

        val classDefinition =
          ClassDefinitionBlock(
            name = DefinitionName("SomeClass"),
            modifier = Modifiers(Seq.empty),
            parents = Parents(Seq.empty),
            pkg = Package("example.somepackage"),
            constructor = Constructor(Seq.empty),
            fileName = fileNameOfClass,
            startLine = 3,
            endLine = 10
          )

        val actual = scaladocs.findDocForDefinition(classDefinition)

        actual shouldBe None
      }
    }

    "there are scaladocs which has same name" should {
      "returns appropriate scaladoc block" in {

        val fileNameOfClass1 = FileName("example/somepackage/SomeClass.scala")
        // name is same with above, package is not
        val fileNameOfClass2 = FileName("example/otherpackage/SomeClass.scala")

        val class1scaladoc = ScaladocBlock(
          fileName = fileNameOfClass1,
          startLine = 10,
          endLine = 20,
          content = "An awesome class1"
        )
        val class2scaladoc = ScaladocBlock(
          fileName = fileNameOfClass2,
          startLine = 10,
          endLine = 20,
          content = "An awesome class2"
        )

        val scaladocs = Seq(class1scaladoc, class2scaladoc).pipe(Scaladocs(_))

        val classDefinition =
          ClassDefinitionBlock(
            name = DefinitionName("SomeClass"),
            modifier = Modifiers(Seq.empty),
            parents = Parents(Seq.empty),
            pkg = Package("example.otherpackage"),
            constructor = Constructor(Seq.empty),
            fileName = fileNameOfClass2,
            startLine = 21,
            endLine = 40
          )

        val actual = scaladocs.findDocForDefinition(classDefinition)

        actual.value shouldBe class2scaladoc
      }
    }
  }
}

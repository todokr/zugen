package tool

import java.nio.file.Paths

import scala.util.chaining._

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import tool.Config.ClassesPath
import tool.TextDocLoader.SemanticdbDirectoryNotExistException

class TextDocLoaderTest extends AnyWordSpec with Matchers {

  "TextDocLoader" should {
    "loads Seq[TextDocument] from SemanticDB bynary" in {
      val classesPath = Paths.get("cli/src/test/resources/classes").pipe(ClassesPath(_))

      val actual = TextDocLoader.load(classesPath)

      val expectedUris = Seq(
        "example/src/main/scala/example/domain/product/Product.scala",
        "example/src/main/scala/example/domain/shipping/Shipping.scala",
        "example/src/main/scala/example/domain/order/Order.scala",
        "example/src/main/scala/example/badreference/SomeClass.scala",
        "example/src/main/scala/example/application/Main.scala"
      )

      actual.map(_.uri) should contain theSameElementsAs expectedUris
    }

    "throws exception if classes directory does not exist" in {
      val classesPath = Paths.get("must/not/exist").pipe(ClassesPath(_))
      the[SemanticdbDirectoryNotExistException] thrownBy TextDocLoader.load(classesPath)
    }
  }
}

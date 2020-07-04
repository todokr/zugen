package zugen.sbt

import sbt.{AutoPlugin, Compile, Def, File, IO, Test, inConfig, settingKey, taskKey, _}
import Keys._
import sbt.plugins.JvmPlugin
import zugen.core.Zugen
import zugen.core.config.GenDocumentType.{GenDomainObjectTable, GenDomainRelationDiagram}
import zugen.core.config._

trait PluginInterface {

  // tasks
  val zugen = taskKey[Unit]("Generate zugen documents")

  // settings
  val zugenClassesPath = settingKey[File]("Class directory of the project to generate document")
  val zugenDomainPackages = settingKey[Seq[String]]("Packages contain domain objects")
  val zugenDocumentsToGenerate = settingKey[Seq[GenDocumentType]]("Document types to generate")
  val zugenDocumentPath = settingKey[File]("Directory to output documents")
}

object ZugenPlugin extends AutoPlugin {

  override def requires: Plugins = JvmPlugin
  override def trigger: PluginTrigger = allRequirements

  object autoImport extends PluginInterface
  import autoImport._

  lazy val baseZugenSettings: Seq[Def.Setting[_]] = Seq(
    zugen := {
      compile.value
      val config = Config(
        classesPath = ClassesPath(zugenClassesPath.value.toPath),
        domainPackages = zugenDomainPackages.value.map(DomainPackageName),
        documentsToGenerate = DocumentsToGenerate(zugenDocumentsToGenerate.value),
        documentPath = DocumentPath(zugenDocumentPath.value.toPath)
      )

      IO.createDirectory(zugenDocumentPath.value / "assets")
      val cssIn = getClass.getClassLoader.getResourceAsStream("assets/style.css")
      IO.transfer(cssIn, zugenDocumentPath.value / "assets" / "style.css")

      val generatedPath = Zugen.generateDocs(config)
      generatedPath.pages.foreach { page =>
        println(s"${scala.Console.GREEN}Generated${scala.Console.RESET}: $page")
      }
      println(s"${scala.Console.GREEN}Generated index page${scala.Console.RESET}: ${generatedPath.index}")
    },
    zugenDomainPackages := zugenDomainPackages.?.value.getOrElse(Seq.empty),
    zugenDocumentsToGenerate := Seq(GenDomainObjectTable, GenDomainRelationDiagram),
    zugenClassesPath := classDirectory.value,
    zugenDocumentPath := target.value / "zugen-docs"
  )

  override lazy val projectSettings: Seq[Def.Setting[_]] =
    inConfig(Compile)(baseZugenSettings) ++ inConfig(Test)(baseZugenSettings)
}

/** Keys for zugen sbt plugin.
  *
  * import this in *.scala build setting.
  * `import zugen.sbt.ZugenKeys._`
  */
object ZugenKeys extends PluginInterface

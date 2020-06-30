package zugen

import sbt._, Keys._
import sbt.plugins.JvmPlugin
import zugen.core.Zugen
import zugen.core.config.GenDocumentType.{GenDomainObjectTable, GenDomainRelationDiagram}
import zugen.core.config._

object ZugenPlugin extends AutoPlugin {

  override def trigger = allRequirements
  override def requires = JvmPlugin

  object autoImport {

    // tasks
    val zugen = taskKey[Unit]("Generate zugen documents")
    val exampleTask = taskKey[String]("A task that is automatically imported to the build")

    // settings
    val zugenClassesPath = settingKey[File]("Class directory of the project to generate document")
    val zugenDomainPackages = settingKey[Seq[String]]("Packages contain domain objects")
    val zugenDocumentsToGenerate = settingKey[Seq[GenDocumentType]]("Document types to generate")
    val zugenDocumentPath = settingKey[File]("Directory to output documents")
  }

  import autoImport._

  lazy val baseZugenSettings: Seq[Def.Setting[_]] = Seq(
    exampleTask := "computed from example setting",
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

  override lazy val projectSettings = inConfig(Compile)(baseZugenSettings) ++ inConfig(Test)(baseZugenSettings)
  override lazy val buildSettings = Seq.empty
  override lazy val globalSettings = Seq.empty

}

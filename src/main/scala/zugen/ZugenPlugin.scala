package zugen

import sbt.Keys._
import sbt._

import zugen.core.Zugen
import zugen.core.config.GenDocumentType.{GenDomainObjectTable, GenDomainRelationDiagram}
import zugen.core.config._

object ZugenPlugin extends AutoPlugin {

  object autoImport {

    // tasks
    val zugen = taskKey[Unit]("Generate zugen documents")

    // settings
    val zugenClassesPath = settingKey[File]("Class directory of the project to generate document")
    val zugenDomainPackages = settingKey[Seq[String]]("Packages contain domain objects")
    val zugenDocumentsToGenerate = settingKey[Seq[GenDocumentType]]("Document types to generate")
    val zugenDocumentPath = settingKey[File]("Directory to output documents")

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
      zugenDocumentsToGenerate := Seq(GenDomainObjectTable, GenDomainRelationDiagram),
      zugenClassesPath := classDirectory.value,
      zugenDocumentPath := target.value / "zugen-docs"
    )
  }

  import autoImport._

  // override def requires: Plugins = sbt.plugins.JvmPlugin
  // override def trigger: PluginTrigger = allRequirements
  override def projectSettings: Seq[Def.Setting[_]] =
    inConfig(Compile)(baseZugenSettings) ++ inConfig(Test)(baseZugenSettings)

}

package zugen.plugin

import sbt.{Def, _}
import sbt.Keys._
import io.github.todokr.core.Config.GenDocumentType.{GenDomainObjectTable, GenDomainRelationDiagram}
import io.github.todokr.core.Config.{ClassesPath, DocumentPath, DocumentsToGenerate, DomainPackageName, GenDocumentType}
import io.github.todokr.core.{Config, Zugen}

object SbtZugen extends AutoPlugin {

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
        Zugen.generateDoc(config)
        baseDirectory.value
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

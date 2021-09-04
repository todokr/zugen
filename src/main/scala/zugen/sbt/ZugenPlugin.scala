package zugen.sbt

import java.io.FileNotFoundException
import java.util.Properties

import scala.util.chaining._
import scala.util.{Failure, Success, Try, Using}

import sbt.Keys._
import sbt.plugins.JvmPlugin
import sbt.{AutoPlugin, Compile, Def, IO, Test, inConfig, taskKey, _}
import zugen.core.Zugen
import zugen.core.Zugen.{DependentProject, ProjectId, ProjectStructure, TargetProject}
import zugen.core.config._

trait PluginInterface {
  val zugen = taskKey[Unit]("Generate zugen documents")
  val zugenConfig = taskKey[Config]("Zugen configuration")

  val ztest = taskKey[Unit]("Generate zugen documents")
}

object ZugenPlugin extends AutoPlugin with SlashSyntax {

  override def requires: Plugins = JvmPlugin
  override def trigger: PluginTrigger = allRequirements

  object autoImport extends PluginInterface
  import autoImport._

  lazy val baseZugenSettings: Seq[Def.Setting[_]] = Seq(
    ztest := {
      println("hey, now I'm do nothing")
    },
    zugen := {
      compile.value
      val config = zugenConfig.value
      IO.createDirectory(config.documentPath.value.resolve("assets").toFile)

      Using(getClass.getClassLoader.getResourceAsStream("assets/style.css")) { cssIn =>
        IO.transfer(cssIn, config.documentPath.value.resolve("assets/style.css").toFile)
      }

      val targetProject = TargetProject(ProjectId(thisProject.value.id), thisProject.value.base)
      val dependentProjects = {
        val bs = buildStructure.value
        thisProject.value.dependencies.map { dep =>
          Project.getProject(dep.project, bs)
        }.collect {
          case Some(project) =>
            DependentProject(ProjectId(project.id), project.base)
        }
      }
      val projectStructure = ProjectStructure(targetProject, dependentProjects)

      val generatedPath = Zugen.generateDocs(config, projectStructure)
      generatedPath.pages.foreach { page =>
        println(s"${scala.Console.GREEN}Generated${scala.Console.RESET}: $page")
      }
      println(s"${scala.Console.GREEN}Generated index page${scala.Console.RESET}: ${generatedPath.index}")
    },
    zugenConfig := {
      loadPropertiesFromFile() match {
        case Left(e)      => throw e
        case Right(props) => loadConfig.value.apply(props)
      }
    }
  )

  override lazy val projectSettings: Seq[Def.Setting[_]] =
    inConfig(Compile)(baseZugenSettings) ++ inConfig(Test)(baseZugenSettings)

  private def loadPropertiesFromFile(): Either[Exception, Properties] = {
    val PropFileName = "project/zugen.properties"
    val prop = new java.util.Properties()
    if (file(PropFileName).exists()) {
      IO.load(prop, file(PropFileName))
      if (prop.isEmpty) println(s"${scala.Console.YELLOW}$PropFileName is empty${scala.Console.RESET} ")
      Right(prop)
    } else {
      Left(new FileNotFoundException(s"setting file not found: $PropFileName"))
    }
  }

  private val DocumentsToGenerateKey = "documentsToGenerate"
  private val DomainPackagesKey = "domainPackages"
  private val DomainObjectExcludePatternsKey = "domainObjectExcludePatterns"
  private val MethodInvocationRootPackageKey = "methodInvocationRootPackage"
  private val DocumentPathKey = "documentPath"
  private val GithubBaseUrlKey = "githubBaseUrl"

  private def loadConfig: Def.Initialize[Task[Properties => Config]] =
    Def.task { prop =>
      val documentsToGenerate =
        getStringList(prop, DocumentsToGenerateKey)
          .map(GenDocumentType.from)
          .collect { case Some(x) => x } match {
          case xs if xs.isEmpty => DocumentsToGenerate(GenDocumentType.values)
          case xs               => DocumentsToGenerate(xs)
        }

      val domainPackages = getStringList(prop, DomainPackagesKey).map(DomainPackageName)
      val domainObjectExcludePatterns = getStringList(prop, DomainObjectExcludePatternsKey)
      val methodInvocationStartingPackage =
        getString(prop, MethodInvocationRootPackageKey).map(MethodInvocationRootPackage)
      val documentPath = getString(prop, DocumentPathKey)
        .getOrElse((target.value / "zugen-docs").toString)
        .pipe(DocumentPath)
      val githubBaseUrl = getString(prop, GithubBaseUrlKey).map { baseUrl =>
        val endSlashRemoved = if (baseUrl.endsWith("/")) baseUrl.init else baseUrl
        Try(new URL(endSlashRemoved)) match {
          case Failure(_)   => throw new Exception(s"URL specified with $GithubBaseUrlKey is not valid.")
          case Success(url) => url
        }
      }

      Config(
        documentsToGenerate = documentsToGenerate,
        domainPackages = domainPackages,
        domainObjectExcludePatterns = domainObjectExcludePatterns,
        methodInvocationStartingPackage = methodInvocationStartingPackage,
        documentPath = documentPath,
        githubBaseUrl = githubBaseUrl
      ).tap { config =>
        println(s"${scala.Console.BLUE}Loaded config${scala.Console.RESET}: $config")
      }
    }

  private def getString(props: Properties, key: String): Option[String] =
    Option(props.get(key)).map { value =>
      val str = value.toString
      if (str.startsWith("\"") && str.endsWith("\"") && str.length >= 2) {
        str.substring(1, str.length - 1)
      } else str
    }

  private def getStringList(props: Properties, key: String): Seq[String] =
    Option(props.get(key)).map { value =>
      value.toString.split(",").map { s =>
        val str = s.trim
        if (str.startsWith("\"") && str.endsWith("\"") && str.length >= 2) {
          str.substring(1, str.length - 1)
        } else str
      }.toSeq
    }.getOrElse(Seq.empty)
}

/** Keys for zugen sbt plugin.
  *
  * import this in *.scala build setting.
  * `import zugen.sbt.ZugenKeys._`
  */
object ZugenKeys extends PluginInterface

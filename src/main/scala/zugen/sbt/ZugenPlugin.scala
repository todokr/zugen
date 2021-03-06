package zugen.sbt

import java.io.FileNotFoundException
import java.nio.file.Files
import java.util.Properties

import scala.util.chaining._
import scala.util.{Failure, Success, Try, Using}

import sbt.Keys._
import sbt.plugins.JvmPlugin
import sbt.{AutoPlugin, Compile, Def, IO, Test, inConfig, taskKey, _}
import zugen.core.Zugen
import zugen.core.Zugen.ProjectStructure
import zugen.core.config._

trait PluginInterface {
  val zugen = taskKey[Unit]("Generate zugen documents")
  val zugenConfig = taskKey[Config]("Zugen configuration")

  val ztest = taskKey[Unit]("Generate zugen documents")
}

object ZugenPlugin extends AutoPlugin {

  override def requires: Plugins = JvmPlugin
  override def trigger: PluginTrigger = allRequirements

  object autoImport extends PluginInterface
  import autoImport._
  /*
-=============================
domain
setting(ScopedKey(This / This / This,target)) at LinePosition((sbt.Defaults.coreDefaultSettings) Defaults.scala,1958)
setting(ScopedKey(This / This / This,target)) at LinePosition((sbt.Defaults.paths) Defaults.scala,379)
setting(ScopedKey(This / Select(ConfigKey(compile)) / Select(doc),target)) at LinePosition((sbt.Defaults.outputConfigPaths) Defaults.scala,470)
setting(ScopedKey(This / Select(ConfigKey(test)) / Select(doc),target)) at LinePosition((sbt.Defaults.outputConfigPaths) Defaults.scala,470)
-=============================
infra
setting(ScopedKey(This / This / This,target)) at LinePosition((sbt.Defaults.coreDefaultSettings) Defaults.scala,1958)
setting(ScopedKey(This / This / This,target)) at LinePosition((sbt.Defaults.paths) Defaults.scala,379)
setting(ScopedKey(This / Select(ConfigKey(compile)) / Select(doc),target)) at LinePosition((sbt.Defaults.outputConfigPaths) Defaults.scala,470)
setting(ScopedKey(This / Select(ConfigKey(test)) / Select(doc),target)) at LinePosition((sbt.Defaults.outputConfigPaths) Defaults.scala,470)
[success] Total time: 0 s, completed 2020/11/25 19:16:58
   */
  lazy val baseZugenSettings: Seq[Def.Setting[_]] = Seq(
    ztest := {
      val bs = buildStructure.value
      val projectDependencies = thisProject.value.dependencies
        .map(dep => Project.getProject(dep.project, bs))
        .collect { case Some(project) => project }

      projectDependencies.foreach { p =>
        import scala.jdk.CollectionConverters._
        Files.walk(p.base.toPath, 5)
          .iterator().asScala.filter { f =>
            Files.isDirectory(f) && f.getFileName.toString == "classes"
          }.foreach(println)
      }

    },
    zugen := {
      compile.value
      val config = zugenConfig.value
      IO.createDirectory(config.documentPath.value.resolve("assets").toFile)

      Using(getClass.getClassLoader.getResourceAsStream("assets/style.css")) { cssIn =>
        IO.transfer(cssIn, config.documentPath.value.resolve("assets/style.css").toFile)
      }

      val bs = buildStructure.value
      val dependencyBaseDirs = thisProject.value.dependencies.map { dep =>
        Project.getProject(dep.project, bs)
      }.collect { case Some(p) => p.base }
      val projectStructure = ProjectStructure.of(dependencyBaseDirs)

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
      if (prop.isEmpty) println(s"${scala.Console.YELLOW}[WARN]${scala.Console.RESET} $PropFileName is empty")
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
  private val ClassesPathKey = "classesPath"
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
      val classesPath = getString(prop, ClassesPathKey).getOrElse(classDirectory.value.toString).pipe(ClassesPath)
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
        classesPath = classesPath,
        githubBaseUrl = githubBaseUrl
      ).tap { config =>
        println(s"${scala.Console.BLUE}[INFO!]${scala.Console.RESET} loaded config: $config")
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

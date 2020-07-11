package zugen.sbt

import java.io.FileNotFoundException
import java.util.Properties

import scala.util.Using
import scala.util.chaining._

import sbt.Keys._
import sbt.plugins.JvmPlugin
import sbt.{AutoPlugin, Compile, Def, IO, Test, inConfig, taskKey, _}
import zugen.core.Zugen
import zugen.core.config._

trait PluginInterface {
  val zugen = taskKey[Unit]("Generate zugen documents")
  val zugenConfig = taskKey[Config]("Zugen configuration")
}

object ZugenPlugin extends AutoPlugin {

  override def requires: Plugins = JvmPlugin
  override def trigger: PluginTrigger = allRequirements

  object autoImport extends PluginInterface
  import autoImport._

  lazy val baseZugenSettings: Seq[Def.Setting[_]] = Seq(
    zugen := {
      compile.value
      val config = zugenConfig.value
      IO.createDirectory(config.documentPath.value.resolve("assets").toFile)

      Using(getClass.getClassLoader.getResourceAsStream("assets/style.css")) { cssIn =>
        IO.transfer(cssIn, config.documentPath.value.resolve("assets/style.css").toFile)
      }

      val generatedPath = Zugen.generateDocs(config)
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
  private val DomainObjectExcludePatterns = "domainObjectExcludePatterns"
  private val DocumentPathKey = "documentPath"
  private val ClassesPathKey = "classesPath"

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
      val domainObjectExcludePatterns = getStringList(prop, DomainObjectExcludePatterns)
      val documentPath = getString(prop, DocumentPathKey)
        .getOrElse((target.value / "zugen-docs").toString)
        .pipe(DocumentPath)
      val classesPath = getString(prop, ClassesPathKey).getOrElse(classDirectory.value.toString).pipe(ClassesPath)

      Config(
        documentsToGenerate = documentsToGenerate,
        domainPackages = domainPackages,
        domainObjectExcludePatterns = domainObjectExcludePatterns,
        documentPath = documentPath,
        classesPath = classesPath
      ).tap { config =>
        println(s"${scala.Console.BLUE}[INFO]${scala.Console.RESET} loaded config: $config")
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

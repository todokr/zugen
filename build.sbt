lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin, SbtTwirl)
  .settings(
    name := "sbt-zugen",
    organization := "io.github.todokr",
    version := "0.0.1-SNAPSHOT",
    scriptedLaunchOpts += ("-Dplugin.version=" + version.value),
    scriptedBufferLog := false,
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-unchecked",
      "-Xlint",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-unused",
      "-language:existentials",
      "-language:higherKinds",
      "-language:implicitConversions"
    ),
    libraryDependencies ++= Seq(
      "org.scalameta" %% "scalameta" % "4.3.17",
      "com.github.bigwheel" %% "util-backports" % "2.1",
      "org.scalatest" %% "scalatest" % "3.2.0" % Test,
      "org.scalacheck" %% "scalacheck" % "1.14.1" % Test
    ),
    TwirlKeys.templateImports := Seq.empty
  )

val docDir = file("zugen-docs")
commands ++= Seq(
  Command.command("runAll") { s =>
    val classesDir = file("src/sbt-test/example/target/scala-2.13/classes")
    val targetPackages = "example.domain"
    s"runMain zugen.core.Main $classesDir $docDir $targetPackages" ::
      "copyAssets" ::
      s
  },
  Command.command("copyAssets") { s =>
    val fromDirectory = resourceDirectory.in(root, Compile).value / "assets"
    val toDirectory = docDir / "assets"
    IO.createDirectory(toDirectory)
    IO.copyDirectory(
      fromDirectory,
      toDirectory,
      CopyOptions(overwrite = true, preserveLastModified = false, preserveExecutable = false))
    s
  }
)

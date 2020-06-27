inThisBuild(List(scalaVersion := "2.13.2"))

lazy val cli = project
  .settings(
    scalaVersion := "2.13.2",
    libraryDependencies ++= Seq(
      "org.scalameta" %% "scalameta" % "4.3.17",
      "org.scalatest" %% "scalatest" % "3.2.0" % Test,
      "org.scalacheck" %% "scalacheck" % "1.14.1" % Test
    ),
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
    TwirlKeys.templateImports := Seq.empty
  )
  .enablePlugins(SbtTwirl)

val docPath = file("example/zugen-docs")
commands ++= Seq(
  Command.command("runAll") { s =>
    val targetRootPath = baseDirectory.in(example, Compile).value
    "example/compile" ::
      s"cli/run $targetRootPath $docPath" ::
      "copyAssets" ::
      s
  },
  Command.command("copyAssets") { s =>
    val fromDirectory = resourceDirectory.in(cli, Compile).value / "assets"
    val toDirectory = docPath / "assets"
    IO.createDirectory(toDirectory)
    IO.copyDirectory(
      fromDirectory,
      toDirectory,
      CopyOptions(overwrite = true, preserveLastModified = false, preserveExecutable = false))
    s
  }
)

lazy val example = project
  .settings(
    addCompilerPlugin(
      "org.scalameta" %% "semanticdb-scalac" % "4.3.17" cross CrossVersion.full
    ),
    scalacOptions += "-Yrangepos"
  )

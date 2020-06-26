inThisBuild(List(scalaVersion := "2.12.4"))

lazy val example = project
  .settings(
    addCompilerPlugin(
      "org.scalameta" % "semanticdb-scalac" % "3.7.4" cross CrossVersion.full
    ),
    scalacOptions += "-Yrangepos"
  )

lazy val cli = project
  .settings(
    libraryDependencies += "org.scalameta" %% "scalameta" % "4.3.15",
    TwirlKeys.templateImports += "tool._"
  )
  .enablePlugins(SbtTwirl)

val docPath = file("example/zugen-docs")
commands ++= Seq(
  Command.command("runAll") { s =>
    val targetRootPath = classDirectory.in(example, Compile).value
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

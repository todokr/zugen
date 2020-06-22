inThisBuild(List(scalaVersion := "2.12.4"))

commands += Command.command("runAll") { s =>
  val targetRootPath = classDirectory.in(example, Compile).value
  val docPath = baseDirectory.in(example).value / "zugen-docs"
  "example/compile" ::
    s"cli/run $targetRootPath $docPath" ::
    s
}

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

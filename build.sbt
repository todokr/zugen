inThisBuild(List(scalaVersion := "2.12.4"))

commands += Command.command("runAll") { s =>
  val dir = classDirectory.in(example, Compile).value
  "example/compile" ::
    s"cli/run $dir" ::
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
  .settings(libraryDependencies += "org.scalameta" %% "scalameta" % "4.3.15")

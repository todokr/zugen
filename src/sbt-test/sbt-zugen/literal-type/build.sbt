lazy val root = (project in file("."))
  .settings(
    name := "literal-type",
    version := "0.0.1",
    scalaVersion := "2.13.6",
    addCompilerPlugin(
      "org.scalameta" %% "semanticdb-scalac" % "4.4.27" cross CrossVersion.full
    ),
    scalacOptions ++= Seq("-Yrangepos", "-P:semanticdb:text:on"),
    libraryDependencies += "eu.timepit" %% "refined" % "0.9.28"
  )

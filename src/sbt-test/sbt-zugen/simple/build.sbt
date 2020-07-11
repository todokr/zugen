lazy val root = (project in file("."))
  .settings(
    name := "simple",
    version := "0.0.1",
    scalaVersion := "2.13.2",
    addCompilerPlugin(
      "org.scalameta" %% "semanticdb-scalac" % "4.3.17" cross CrossVersion.full
    ),
    scalacOptions ++= Seq("-Yrangepos", "-P:semanticdb:text:on")
  )

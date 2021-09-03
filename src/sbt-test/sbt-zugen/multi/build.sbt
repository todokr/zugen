name := """multi-project application"""
organization := "com.example"
version := "1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.6"
ThisBuild / libraryDependencies += compilerPlugin(
  "org.scalameta" %% "semanticdb-scalac" % "4.4.27" cross CrossVersion.full)
ThisBuild / scalacOptions ++= Seq("-Yrangepos", "-P:semanticdb:text:on")

lazy val application = (project in file("application"))
  .enablePlugins(PlayScala)
  .settings(
    libraryDependencies ++= Seq(
      guice,
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
    )
  )
  .dependsOn(domain, infra)

lazy val domain = (project in file("domain"))
lazy val infra = (project in file("infrastructure"))
  .dependsOn(domain)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"

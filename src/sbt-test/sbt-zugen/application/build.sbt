name := """application"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.6"

libraryDependencies ++= Seq(
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
)
addCompilerPlugin(
  "org.scalameta" %% "semanticdb-scalac" % "4.4.27" cross CrossVersion.full
)
scalacOptions ++= Seq("-Yrangepos", "-P:semanticdb:text:on")
// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"

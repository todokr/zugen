ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := "4.4.27"

lazy val root = (project in file("."))
  .settings(
    name := "sbt-provided-semanticdb",
    version := "0.0.1",
    scalaVersion := "2.13.6",
    scalacOptions ++= Seq("-Yrangepos", "-P:semanticdb:text:on")
  )

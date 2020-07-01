lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin, SbtTwirl)
  .settings(
    name := "sbt-zugen",
    organization := "io.github.todokr",
    version := "0.0.2",
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++
        Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false,
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-unchecked",
      "-Xlint",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-unused"
    ),
    libraryDependencies ++= Seq(
      "org.scalameta" %% "scalameta" % "4.3.17",
      "com.github.bigwheel" %% "util-backports" % "2.1",
      "org.scalatest" %% "scalatest" % "3.2.0" % Test,
      "org.scalacheck" %% "scalacheck" % "1.14.1" % Test
    ),
    TwirlKeys.templateImports := Seq.empty
  )

description := "A tool for generating Zu(図) of Scala project architecture"
licenses := List("EPL 2.0" -> new URL("https://www.eclipse.org/legal/epl-2.0/"))
homepage := Some(url("https://github.com/todokr/zugen"))
developers := List(
  Developer(
    id = "todokr",
    name = "Shunsuke Tadokoro",
    email = "s.tadokoro0317@gmail.com",
    url = url("https://github.com/todokr")
  )
)
scmInfo := Some(
  ScmInfo(
    url("https://github.com/todokr/zugen"),
    "scm:git@github.com:todokr/zugen.git"
  )
)
pomIncludeRepository := { _ => false }
publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
publishMavenStyle := true

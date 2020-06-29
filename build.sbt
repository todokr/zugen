lazy val plugin = (project in file("plugin"))
  .enablePlugins(SbtPlugin)
  .settings(
    organization := "io.github.todokr",
    organizationName := "todokr",
    name := "sbt-zugen",
    version := "0.0.1-SNAPSHOT",
    scriptedLaunchOpts += ("-Dplugin.version=" + version.value),
    scriptedBufferLog := false,
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
    )
  ).dependsOn(core)

lazy val core = (project in file("core"))
  .settings(
    scalaVersion := "2.12.10",
    organization := "io.github.todokr",
    organizationName := "todokr",
    name := "zugen-core",
    libraryDependencies ++= Seq(
      "org.scalameta" %% "scalameta" % "4.3.17",
      "com.github.bigwheel" %% "util-backports" % "2.1",
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

val docDir = file("zugen-docs")
commands ++= Seq(
  Command.command("runAll") { s =>
    val classesDir = file("plugin/src/sbt-test/example/target/scala-2.13/classes")
    val targetPackages = "example.domain"
    s"core/runMain io.github.todokr.zugen.core.Main $classesDir $docDir $targetPackages" ::
      "copyAssets" ::
      s
  },
  Command.command("copyAssets") { s =>
    val fromDirectory = resourceDirectory.in(core, Compile).value / "assets"
    val toDirectory = docDir / "assets"
    IO.createDirectory(toDirectory)
    IO.copyDirectory(
      fromDirectory,
      toDirectory,
      CopyOptions(overwrite = true, preserveLastModified = false, preserveExecutable = false))
    s
  }
)

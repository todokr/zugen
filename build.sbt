lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin, SbtTwirl)
  .settings(
    name := "sbt-zugen",
    organization := "io.github.todokr",
    version := "0.0.1-SNAPSHOT",
    //sbtPlugin := true,
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

val docDir = file("zugen-docs")
commands ++= Seq(
  Command.command("runAll") { s =>
    val classesDir = file("src/sbt-test/sbt-zugen/simple/target/scala-2.13/classes")
    val targetPackages = "example.domain"
    s"runMain zugen.core.Main $classesDir $docDir $targetPackages" :: s
  }
)

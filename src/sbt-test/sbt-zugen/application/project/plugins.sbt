addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.2")
addSbtPlugin("org.foundweekends.giter8" % "sbt-giter8-scaffold" % "0.11.0")
sys.props.get("plugin.version") match {
  case Some(x) => addSbtPlugin("io.github.todokr" % "sbt-zugen" % x)
  case _       => sys.error("""|The system property 'plugin.version' is not defined.
-                              |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
}

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.8")
sys.props.get("plugin.version") match {
  case Some(x) => addSbtPlugin("io.github.todokr" % "sbt-zugen" % x)
  case _       => addSbtPlugin("io.github.todokr" % "sbt-zugen" % "2021.12.0")
}

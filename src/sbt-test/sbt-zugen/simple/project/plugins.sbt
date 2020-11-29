sys.props.get("plugin.version") match {
  case Some(x) => addSbtPlugin("io.github.todokr" % "sbt-zugen" % x)
  case _       => addSbtPlugin("io.github.todokr" % "sbt-zugen" % "2020.11.1-SNAPSHOT")
}

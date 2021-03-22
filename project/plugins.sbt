logLevel := Level.Warn

// addCompilerPlugin(
  // "org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full
  // "org.scalamacros" % "paradise" % "2.1.1" 
// )

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")

addSbtPlugin("no.arktekk.sbt" % "aether-deploy" % "0.22.1")

addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "1.5.0")

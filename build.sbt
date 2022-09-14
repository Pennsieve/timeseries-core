name := "timeseries-core"

organization := "com.pennsieve"

scalaVersion := "2.12.7"

scalacOptions += "-feature"

version := sys.props.get("version").getOrElse("SNAPSHOT")

val pennsieveCoreVersion = "com.pennsieve-SNAPSHOT"

resolvers ++= Seq(
  "Spray" at "https://repo.spray.io",
  Resolver.bintrayRepo("commercetools", "maven"),
  Resolver.typesafeRepo("releases"),
  "JBoss" at "https://repository.jboss.org/",
  "Pennsieve Maven Proxy" at "https://nexus.pennsieve.cc/repository/maven-public",
  "Pennsieve Snapshots" at "https://nexus.pennsieve.cc/repository/maven-snapshots",
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases")
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.6.5",
  "org.postgresql" % "postgresql" % "9.4-1200-jdbc41",
  "org.scalikejdbc" %% "scalikejdbc" % "2.5.0",
  "org.scalikejdbc" %% "scalikejdbc-config" % "2.5.0",
  "org.scalatest" %% "scalatest" % "3.0.3" % "test",
  "org.scalikejdbc" %% "scalikejdbc-test" % "2.5.0" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.4.2" % "test",
  "com.dimafeng" %% "testcontainers-scala" % "0.38.4" % "test",
  "com.pennsieve" %% "pennsieve-core" % s"$pennsieveCoreVersion" % "test" classifier "tests"
)

publishMavenStyle := true

publishTo := {
  val nexus = "https://nexus.pennsieve.cc/repository"
  if (isSnapshot.value) {
    Some("Nexus Realm" at s"$nexus/maven-snapshots")
  } else {
    Some("Nexus Realm" at s"$nexus/maven-releases")
  }
}

credentials += Credentials(
  "Sonatype Nexus Repository Manager",
  "nexus.pennsieve.cc",
  sys.env("PENNSIEVE_NEXUS_USER"),
  sys.env("PENNSIEVE_NEXUS_PW")
)

Test / logBuffered := false

Test / publishArtifact := true

addCompilerPlugin("org.psywerx.hairyfotr" %% "linter" % "0.1.17")

Compile / PB.targets := Seq(
  scalapb.gen(flatPackage = true) -> (sourceManaged in Compile).value
)

// sbt-docker configuration
enablePlugins(sbtdocker.DockerPlugin)

docker / buildOptions := BuildOptions(
  cache = false
)

docker / dockerfile := {
  val jarFile: File = (Compile / packageBin / sbt.Keys.`package`).value
  //val jarFile: File = sbt.Keys.`package`.in(Compile, packageBin).value
  new Dockerfile {
    from("java")
    copy(jarFile, "target/scala-2.11/timeseries-core_2.11-1.1.1-SNAPSHOT.jar")
  }
}

docker / imageNames := Seq(
  ImageName(s"${organization.value}/api:latest"),
  ImageName(
    s"${organization.value}/api:${sys.props.getOrElse("docker-version", version.value)}"
  )
)

name := "timeseries-core"

organization := "com.blackfynn"

scalaVersion := "2.12.7"

val isProd = sys.props
  .get("build-env")
  .exists(_ == "prod")

def envVersion(version: String): String = {
  if (isProd) version
  else s"$version-SNAPSHOT"
}

version := envVersion("1.2.13")

isSnapshot := !isProd

val pennsieveCoreVersion = "bootstrap-SNAPSHOT"

resolvers ++= Seq(
  "Spray" at "https://repo.spray.io",
  Resolver.bintrayRepo("commercetools", "maven"),
  Resolver.typesafeRepo("releases"),
  "JBoss" at "https://repository.jboss.org/",
  "pennsieve-maven-proxy" at "https://nexus.pennsieve.cc/repository/maven-public",
  Resolver.url(
    "pennsieve-ivy-proxy",
    url("https://nexus.pennsieve.cc/repository/ivy-public/")
  )(
    Patterns(
      "[organization]/[module]/(scala_[scalaVersion]/)(sbt_[sbtVersion]/)[revision]/[type]s/[artifact](-[classifier]).[ext]"
    )
  ),
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
  "com.blackfynn" %% "pennsieve-core" % s"$pennsieveCoreVersion" % "test" classifier "tests"
)

publishMavenStyle := true

publishTo := {
  val nexus = "https://nexus.pennsieve.cc/repository"
  if (isProd) {
    Some("Nexus Realm" at s"$nexus/maven-releases")
  } else {
    Some("Nexus Realm" at s"$nexus/maven-snapshots")
  }
}

credentials += Credentials(
  "Sonatype Nexus Repository Manager",
  "nexus.pennsieve.cc",
  sys.env
    .get("PENNSIEVE_NEXUS_USER")
    .getOrElse("pennsieveci"),
  sys.env
    .get("PENNSIEVE_NEXUS_PW")
    .getOrElse("")
)

logBuffered in Test := false

publishArtifact in Test := true

addCompilerPlugin("org.psywerx.hairyfotr" %% "linter" % "0.1.17")

PB.targets in Compile := Seq(
  scalapb.gen(flatPackage = true) -> (sourceManaged in Compile).value
)

// sbt-docker configuration
enablePlugins(sbtdocker.DockerPlugin)

buildOptions in docker := BuildOptions(
  cache = false
)

dockerfile in docker := {
  val jarFile: File = sbt.Keys.`package`.in(Compile, packageBin).value
  new Dockerfile {
    from("java")
    copy(jarFile, "target/scala-2.11/timeseries-core_2.11-1.1.1-SNAPSHOT.jar")
  }
}

imageNames in docker := Seq(
  ImageName(s"${organization.value}/api:latest"),
  ImageName(
    s"${organization.value}/api:${sys.props.getOrElse("docker-version", version.value)}"
  )
)

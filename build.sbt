name := "timeseries-core"

organization := "com.pennsieve"

lazy val scala212 = "2.12.7"
lazy val scala213 = "2.13.8"
lazy val supportedScalaVersions = List(scala212, scala213)

scalaVersion := scala212

crossScalaVersions := supportedScalaVersions

version := sys.props.get("version").getOrElse("SNAPSHOT")

scalacOptions ++= Seq("-deprecation", "-feature")

resolvers ++= Seq(
  Resolver.typesafeRepo("releases"),
  "JBoss" at "https://repository.jboss.org/",
  "Pennsieve Maven Proxy" at "https://nexus.pennsieve.cc/repository/maven-public",
  "Pennsieve Snapshots" at "https://nexus.pennsieve.cc/repository/maven-snapshots",
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases")
)

lazy val pennsieveCoreVersion = "195-8c3149d"
lazy val scalikejdbcVersion = "3.5.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.6.5",
  "org.postgresql" % "postgresql" % "9.4-1200-jdbc41",
  "org.scalikejdbc" %% "scalikejdbc" % scalikejdbcVersion,
  "org.scalikejdbc" %% "scalikejdbc-config" % scalikejdbcVersion,
  "org.scalatest" %% "scalatest" % "3.2.12" % "test",
  "org.scalikejdbc" %% "scalikejdbc-test" % scalikejdbcVersion % "test",
  "com.dimafeng" %% "testcontainers-scala" % "0.40.1" % "test",
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

Compile / PB.targets := Seq(
  scalapb.gen(flatPackage = true) -> (Compile / sourceManaged).value
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

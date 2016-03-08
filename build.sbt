name := """housekeeper"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint:_",
  "-Yno-adapted-args",
  "-encoding", "UTF-8"
)

// sbt settings
incOptions := incOptions.value.withNameHashing(true)
updateOptions := updateOptions.value.withCachedResolution(true)

val playSlickVersion = "1.0.1"

val slickVersion = "3.0.0"

val macwireVersion = "1.0.1"

resolvers ++= Seq(
  "Atlassian Releases" at "https://maven.atlassian.com/public/",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Scalaz Bintray Repository" at "https://dl.bintray.com/scalaz/releases",
  Resolver.jcenterRepo
)

libraryDependencies ++= Seq(
  cache,
  filters,
  ws,
  "com.mohiva" %% "play-silhouette" % "3.0.4",
  "org.webjars" %% "webjars-play" % "2.4.0-1",
  "com.adrianhurt" %% "play-bootstrap3" % "0.4.3-P24-SNAPSHOT",
  "com.softwaremill.macwire" %% "macros" % macwireVersion,
  "com.softwaremill.macwire" %% "runtime" % macwireVersion,
  "org.mindrot" % "jbcrypt" % "0.3m",
  "com.typesafe.slick" %% "slick" % slickVersion,
  "com.typesafe.play" %% "play-slick" % playSlickVersion,
  "com.typesafe.play" %% "play-slick-evolutions" % playSlickVersion,
  "com.iheart" %% "ficus" % "1.2.0",
  "org.slf4j" % "slf4j-nop" % "1.7.12",
  "com.h2database" % "h2" % "1.4.187",
  "org.postgresql" % "postgresql" % "9.4-1203-jdbc42"
)

libraryDependencies ++= Seq(
  specs2,
  "com.mohiva" %% "play-silhouette-testkit" % "3.0.4"
).map(_ % "test")

routesGenerator := play.routes.compiler.InjectedRoutesGenerator

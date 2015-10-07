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

// we are using local build of play-slick, because evolutions aren't possible until play-slick#269 is solved
val playSlickVersion = "1.0.1"

val slickVersion = "3.0.0"

val macwireVersion = "1.0.1"

resolvers ++= Seq(
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Scalaz Bintray Repository" at "https://dl.bintray.com/scalaz/releases")

libraryDependencies ++= Seq(
  cache,
  ws,
  "org.webjars" %% "webjars-play" % "2.4.0-1",
  "com.adrianhurt" %% "play-bootstrap3" % "0.4.3-P24-SNAPSHOT",
  "com.softwaremill.macwire" %% "macros" % macwireVersion,
  "com.softwaremill.macwire" %% "runtime" % macwireVersion,
  "org.mindrot" % "jbcrypt" % "0.3m",
  "com.typesafe.slick" %% "slick" % slickVersion,
  "com.typesafe.play" %% "play-slick" % playSlickVersion,
  "com.typesafe.play" %% "play-slick-evolutions" % playSlickVersion,
  "org.slf4j" % "slf4j-nop" % "1.7.12",
  "com.h2database" % "h2" % "1.4.187",
  "org.postgresql" % "postgresql" % "9.4-1203-jdbc42"
)

libraryDependencies ++= Seq(
  specs2
).map(_ % "test")

routesGenerator := play.routes.compiler.InjectedRoutesGenerator

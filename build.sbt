name := """housekeeper"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

val playSlickVersion = "1.0.0-RC3"

val slickVersion = "3.0.0"

val macwireVersion = "1.0.1"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  cache,
  ws,
  "org.webjars" %% "webjars-play" % "2.4.0",
  "com.adrianhurt" %% "play-bootstrap3" % "0.4.3-P24-SNAPSHOT",
  "com.softwaremill.macwire" %% "macros" % macwireVersion,
  "com.softwaremill.macwire" %% "runtime" % macwireVersion,
  "org.mindrot" % "jbcrypt" % "0.3m",
  "com.typesafe.slick" %% "slick" % slickVersion,
  "com.typesafe.play" %% "play-slick" % playSlickVersion,
  "com.typesafe.play" %% "play-slick-evolutions" % playSlickVersion,
  "org.slf4j" % "slf4j-nop" % "1.7.12"
)

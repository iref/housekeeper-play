name := """housekeeper"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.webjars" %% "webjars-play" % "2.3.0-2",
  "org.webjars" % "bootstrap" % "3.1.1-2",
  "com.adrianhurt" %% "play-bootstrap3" % "0.4",
  "com.softwaremill.macwire" %% "macros" % "0.8.0",
  "com.softwaremill.macwire" %% "runtime" % "0.8.0",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "com.typesafe.slick" %% "slick" % "2.1.0",
  "com.typesafe.play" %% "play-slick" % "0.8.1"
)

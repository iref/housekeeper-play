import com.typesafe.sbt.SbtScalariform.{ScalariformKeys, _}
import sbt.Keys._
import scalariform.formatter.preferences._


lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(commonSettings)
  .settings(playSettings)
  .settings(scoverageSettings)
  .settings(scalariformSettings)

lazy val commonSettings = Seq(
  name := """housekeeper""",
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.11.8",
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint:_",
    "-Yno-adapted-args",
    "-encoding", "UTF-8"
  ),
  scapegoatVersion := "1.1.0",
  // sbt settings
  incOptions := incOptions.value.withNameHashing(true),
  updateOptions := updateOptions.value.withCachedResolution(true),

  // dependencies
  resolvers ++= Seq(
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
    "Scalaz Bintray Repository" at "https://dl.bintray.com/scalaz/releases"),
  libraryDependencies ++= dependencies
)

lazy val scalariformSettings = SbtScalariform.scalariformSettings ++ Seq(
  ScalariformKeys.preferences := ScalariformKeys.preferences.value
    .setPreference(AlignSingleLineCaseStatements, true)
    .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
    .setPreference(PreserveSpaceBeforeArguments, true)
    .setPreference(DanglingCloseParenthesis, Preserve)
    .setPreference(DoubleIndentClassDeclaration, true)
    .setPreference(SpacesAroundMultiImports, false)
)

lazy val scoverageSettings = Seq(
  coverageMinimum := 60,
  coverageFailOnMinimum := true,
  coverageHighlighting := scalaBinaryVersion.value != "2.10"
)

lazy val playSettings = Seq(
  routesGenerator := play.routes.compiler.InjectedRoutesGenerator
)

val playSlickVersion = "2.0.0"

val slickVersion = "3.1.1"

lazy val dependencies = Seq(
  cache,
  ws,
  "org.webjars"              %% "webjars-play"          % "2.5.0-3",
  "com.adrianhurt"           %% "play-bootstrap"        % "1.1-P25-B3",
  "com.softwaremill.macwire" %% "macros"                % "2.2.5",
  "org.mindrot"               % "jbcrypt"               % "0.3m",
  "com.typesafe.slick"       %% "slick"                 % slickVersion,
  "com.typesafe.play"        %% "play-slick"            % playSlickVersion,
  "com.typesafe.play"        %% "play-slick-evolutions" % playSlickVersion,
  "org.typelevel"            %% "cats-core"             % "0.7.2",
  "org.slf4j"                 % "slf4j-api"             % "1.7.12",
  "org.postgresql"            % "postgresql"            % "9.4.1211",
  "com.h2database"            % "h2"                    % "1.4.192"         % "test",
  specs2                                                                    % "test"
)
import sbt.Keys._
import sbt._
import com.typesafe.sbt.pgp.PgpKeys

object Scoverage extends Build {

  val Org = "org.scoverage"
  val ScalaVersion = "2.12.0-M3"
  val MockitoVersion = "1.9.5"
  val ScalatestVersion = "3.0.0-M12"

  lazy val LocalTest = config("local") extend Test

  val appSettings = Seq(
    organization := Org,
    scalaVersion := ScalaVersion,
    crossScalaVersions := Seq("2.10.6", "2.11.7", ScalaVersion),
    fork in Test := false,
    publishMavenStyle := true,
    publishArtifact in Test := false,
    parallelExecution in Test := false,
    sbtrelease.ReleasePlugin.autoImport.releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    sbtrelease.ReleasePlugin.autoImport.releaseCrossBuild := true,
    scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8"),
    resolvers := ("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2") +: resolvers.value,
    concurrentRestrictions in Global += Tags.limit(Tags.Test, 1),
    javacOptions := Seq("-source", "1.6", "-target", "1.6"),
    libraryDependencies ++= Seq(
      "org.mockito"         % "mockito-all" % MockitoVersion % "test",
      "org.scalatest"       %% "scalatest" % ScalatestVersion % "test"
    ),
    publishTo <<= version {
      (v: String) =>
        val nexus = "https://oss.sonatype.org/"
        if (v.trim.endsWith("SNAPSHOT"))
          Some(Resolver.sonatypeRepo("snapshots"))
        else
          Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    pomExtra := {
      <url>https://github.com/scoverage/scalac-scoverage-plugin</url>
        <licenses>
          <license>
            <name>Apache 2</name>
            <url>http://www.apache.org/licenses/LICENSE-2.0</url>
            <distribution>repo</distribution>
          </license>
        </licenses>
        <scm>
          <url>git@github.com:scoverage/scalac-scoverage-plugin.git</url>
          <connection>scm:git@github.com:scoverage/scalac-scoverage-plugin.git</connection>
        </scm>
        <developers>
          <developer>
            <id>sksamuel</id>
            <name>Stephen Samuel</name>
            <url>http://github.com/sksamuel</url>
          </developer>
        </developers>
    },
    pomIncludeRepository := {
      _ => false
    }
  )

  lazy val root = Project("scalac-scoverage", file("."))
    .settings(name := "scalac-scoverage")
    .settings(appSettings: _*)
    .settings(publishArtifact := false)
    .aggregate(plugin, runtime)

  lazy val runtime = Project("scalac-scoverage-runtime", file("scalac-scoverage-runtime"))
    .settings(name := "scalac-scoverage-runtime")
    .settings(appSettings: _*)

  lazy val plugin = Project("scalac-scoverage-plugin", file("scalac-scoverage-plugin"))
    .dependsOn(runtime % "test")
    .settings(name := "scalac-scoverage-plugin")
    .settings(appSettings: _*)
    .settings(libraryDependencies ++= Seq(
    "org.scala-lang"                % "scala-reflect"             % scalaVersion.value    % "provided",
    "org.scala-lang"                % "scala-compiler"            % scalaVersion.value    % "provided",
    "org.joda"                      %  "joda-convert"             % "1.6"       % "test",
    "joda-time"                     %  "joda-time"                % "2.3"       % "test"
  )).settings(libraryDependencies ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, scalaMajor)) if scalaMajor == 12 =>
        EnvSupport.setEnv("CrossBuildScalaVersion", "2.12.0-M3")
        Seq("org.scala-lang.modules" % "scala-xml_2.12.0-M3" % "1.0.5")
      case Some((2, scalaMajor)) if scalaMajor == 11 =>
        EnvSupport.setEnv("CrossBuildScalaVersion", "2.11.7")
        Seq("org.scala-lang.modules" % "scala-xml_2.11" % "1.0.5")
      case _ =>
        EnvSupport.setEnv("CrossBuildScalaVersion", "2.10.6")
        Nil
    }
  })
}
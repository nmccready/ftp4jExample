import sbt._
import Keys._

object Build extends sbt.Build {

  import Dependencies._

  lazy val myProject = Project("ftp4jExample", file("."))
    .settings(
    organization := "com.nmccready",
    version := "1.0-SNAPSHOT",
    scalaVersion := Dependencies.V.thisScala,
    crossScalaVersions := Seq(Dependencies.V.thisScala, Dependencies.V.newerScala),
    scalacOptions := Seq("-deprecation", "-encoding", "utf8"),
    //resolvers ++= Dependencies.resolutionRepos,
    libraryDependencies ++= (compileDeps ++ testDeps),
    testOptions in Test += Tests.Argument(TestFrameworks.Specs2, "junitxml", "console")
  )
}

object Dependencies {
  //val resolutionRepos = Seq("")

  object V {
    val slf4j = "1.6.6"
    val logback = "1.0.7"
    val specs2 = "1.12.1"
    val junit = "4.10"
    //Scala Versions
    val oldScala = "2.9.1"
    val thisScala = "2.9.2"
    val newerScala = "2.9.2"
  }

  val compileDeps: Seq[ModuleID] = Seq(
    "junit" % "junit" % V.junit,
    "org.slf4j" % "slf4j-api" % V.slf4j
  )

  val testDeps: Seq[ModuleID] = Seq(
    "org.slf4j" % "slf4j-api" % V.slf4j,
    "ch.qos.logback" % "logback-classic" % V.logback
  )

  def withOld(namespace: String, version: String): String = appendAll(namespace, "_", version)

  def appendAll(strings: String*) = {
    val sb = new StringBuilder()
    strings.foreach(s => sb.append(s))
    sb.toString
  }
}
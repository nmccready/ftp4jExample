import sbt._
import Keys._

object Build extends sbt.Build {

  import Dependencies._

  lazy val myProject = Project("ftp4jExample", file(".")) //,aggregate = Seq(GitHubUris.ftp4j))
    .settings(
    organization := "com.nmccready",
    version := "1.0-SNAPSHOT",
    scalaVersion := V.thisScala,
    crossScalaVersions := Seq(V.thisScala, V.newerScala),
    scalacOptions := Seq("-deprecation", "-encoding", "utf8"),
    libraryDependencies ++= (compileDeps ++ testDeps),
    testOptions in Test += Tests.Argument(TestFrameworks.Specs2, "junitxml", "console")
  )
}

object Dependencies {

  object GitHubUris {
    val ftp4j = uri("git://github.com/nmccready/ftp4j.git")
    //val ftp4j = file("../lib/ftp4j-1.7.2.jar")
  }

  object V {
    val master = "master"
    val slf4j = "1.6.6"
    val specs2 = "1.12.1"
    val junit = "4.10"
    val ftp4j = "1.7.2"
    //Scala Versions
    val oldScala = "2.9.1"
    val thisScala = "2.9.2"
    val newerScala = "2.9.2"
  }

  val compileDeps: Seq[ModuleID] = Seq(
    "junit" % "junit" % V.junit,
    "org.slf4j" % "slf4j-api" % V.slf4j,
    "org.specs2" %% "specs2" % V.specs2,
    "it.sauronsoftware" %% "ftp4j" % V.ftp4j
      from "http://sourceforge.net/projects/ftp4j/files/ftp4j/1.7.2/ftp4j-1.7.2.zip/download"
  )

  val testDeps: Seq[ModuleID] = Seq(
    "junit" % "junit" % V.junit,
    "org.slf4j" % "slf4j-api" % V.slf4j,
    "org.specs2" %% "specs2" % V.specs2,
    "it.sauronsoftware" %% "ftp4j" % V.ftp4j
      from "http://sourceforge.net/projects/ftp4j/files/ftp4j/1.7.2/ftp4j-1.7.2.zip/download"
  )

  def withOld(namespace: String, version: String): String = appendAll(namespace, "_", version)

  def appendAll(strings: String*) = {
    val sb = new StringBuilder()
    strings.foreach(s => sb.append(s))
    sb.toString
  }
}
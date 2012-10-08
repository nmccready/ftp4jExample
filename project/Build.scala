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
    testOptions in Test += Tests.Argument(TestFrameworks.Specs2, "junitxml", "console"),
    resolvers ++= Dependencies.resolutionRepos
  )
}

object Dependencies {
  val resolutionRepos = Seq(
    "akkaRekeases" at "http://repo.akka.io/releases",
    "sonatype" at "http://oss.sonatype.org/content/repositories/releases",
    "releases" at "http://oss.sonatype.org/content/repositories/releases"
  )
  object GitHubUris {
    val ftp4j = uri("git://github.com/nmccready/ftp4j.git")
    //val ftp4j = file("../lib/ftp4j-1.7.2.jar")
  }

  object V {
    val master = "master"
    val slf4s = "1.0.7"
    val specs2 = "1.12.1"
    val junit = "4.10"
    val ftp4j = "1.7.2"
    val typeSafeConfig = "0.5.2"
    val akka = "2.0.3"
    //Scala Versions
    val oldScala = "2.9.1"
    val thisScala = "2.9.2"
    val newerScala = "2.9.2"
  }

  val compileDeps: Seq[ModuleID] = Seq(
    "junit" % "junit" % V.junit,
    "com.weiglewilczek.slf4s" % withOld("slf4s",V.oldScala) % V.slf4s,
    "org.specs2" %% "specs2" % V.specs2,
    "it.sauronsoftware" %% "ftp4j" % V.ftp4j
      from "http://sourceforge.net/projects/ftp4j/files/ftp4j/1.7.2/ftp4j-1.7.2.zip/download",
    "com.typesafe" % "config" % V.typeSafeConfig,
    "com.typesafe.akka" % "akka-actor" % V.akka
  )

  val testDeps: Seq[ModuleID] = Seq(
    "junit" % "junit" % V.junit,
    "com.weiglewilczek.slf4s" % withOld("slf4s",V.oldScala) % V.slf4s,
    "org.specs2" %% "specs2" % V.specs2,
    "it.sauronsoftware" %% "ftp4j" % V.ftp4j
      from "http://sourceforge.net/projects/ftp4j/files/ftp4j/1.7.2/ftp4j-1.7.2.zip/download",
    "com.typesafe" % "config" % V.typeSafeConfig,
    "com.typesafe.akka" % "akka-actor" % V.akka
  )

  def withOld(namespace: String, version: String): String = appendAll(namespace, "_", version)

  def appendAll(strings: String*) = {
    val sb = new StringBuilder()
    strings.foreach(s => sb.append(s))
    sb.toString
  }
}
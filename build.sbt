name := "little-config"
version := "0.1.0"
organization := "com.github.losizm"

scalaVersion := "2.12.6"
scalacOptions ++= Seq("-deprecation", "-feature", "-Xcheckinit")

libraryDependencies ++= Seq(
  "com.typesafe"  %  "config"    % "1.3.3",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)

scmInfo := Some(
  ScmInfo(
    url("https://github.com/losizm/little-config"),
    "scm:git@github.com:losizm/little-config.git"
  )
)

developers := List(
  Developer(
    id    = "losizm",
    name  = "Carlos Conyers",
    email = "carlos.conyers@hotmail.com",
    url   = url("https://github.com/losizm")
  )
)

description := "Scala library that provides extension methods to com.typesafe.config"
licenses := List("Apache License, Version 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
homepage := Some(url("https://github.com/losizm/little-config"))

pomIncludeRepository := { _ => false }

publishTo := {
  val nexus = "https://oss.sonatype.org"
  if (isSnapshot.value) Some("snaphsots" at s"$nexus/content/repositories/snapshots")
  else Some("releases" at s"$nexus/service/local/staging/deploy/maven2")
}

publishMavenStyle := true

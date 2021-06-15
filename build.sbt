organization := "com.github.losizm"
name         := "little-config"
version      := "0.7.1-SNAPSHOT"

description  := "The Scala library that provides extension methods to Typesafe Config"
licenses     := List("Apache License, Version 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
homepage     := Some(url("https://github.com/losizm/little-config"))

scalaVersion := "2.13.6"
crossScalaVersions := Seq("2.12.14")

scalacOptions ++= Seq("-deprecation", "-feature", "-Xcheckinit")

Compile / doc / scalacOptions ++= Seq(
  "-doc-title", name.value,
  "-doc-version", version.value
)

Compile / unmanagedSourceDirectories += {
  (Compile / sourceDirectory).value / s"scala-${scalaBinaryVersion.value}"
}

libraryDependencies ++= Seq(
  "com.typesafe"  %  "config"    % "1.3.4" % "provided",
  "org.scalatest" %% "scalatest" % "3.2.9" % "test"
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

pomIncludeRepository := { _ => false }

publishTo := {
  val nexus = "https://oss.sonatype.org"
  isSnapshot.value match {
    case true  => Some("snaphsots" at s"$nexus/content/repositories/snapshots")
    case false => Some("releases" at s"$nexus/service/local/staging/deploy/maven2")
  }
}

publishMavenStyle := true

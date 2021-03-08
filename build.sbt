import Dependencies._

ThisBuild / scalaVersion     := "2.13.4"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

val akka = Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.6.8",
  "com.typesafe.akka" %% "akka-stream" % "2.6.8",
  "com.typesafe.akka" %% "akka-http" % "10.2.4"
)

lazy val root = (project in file("."))
  .settings(
    name := "live-views",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "scalatags" % "0.8.2",
      "org.scalatest" %% "scalatest" % "latest.integration" % "test"
    ) ++ akka
  )

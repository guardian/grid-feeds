name := "grid-feeds"
organization := "com.gu"
scalaVersion := "2.13.10"

routesGenerator := InjectedRoutesGenerator

libraryDependencies += ws

lazy val root = (project in file(".")).enablePlugins(PlayScala)


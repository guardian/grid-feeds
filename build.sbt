name := "grid-feeds"
organization := "com.gu"
scalaVersion := "2.13.10"

routesGenerator := InjectedRoutesGenerator

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4",
  "software.amazon.awssdk" % "s3" % "2.19.24",
  ws
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)

organization := "com.gu"
lazy val scalaVersionSpec = "2.13.10"

lazy val associatedPressFeed = Project("associated-press-feed", file("associated-press"))
  .enablePlugins(PlayScala)
  .settings(
    name := "associated-press-feed",
    ThisBuild / scalaVersion := scalaVersionSpec,
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4",
      "software.amazon.awssdk" % "s3" % "2.19.27",
      ws
    ),
    routesGenerator := InjectedRoutesGenerator,
    PlayKeys.playDefaultPort := 8855
  )

lazy val root = (project in file("."))
  .aggregate(associatedPressFeed)
  .settings(
    run / aggregate := true
  )
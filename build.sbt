organization := "com.gu"
lazy val scalaVersionSpec = "2.13.10"

lazy val associatedPressFeed = Project("associated-press-feed", file("associated-press"))
  .enablePlugins(PlayScala)
  .settings(
    name := "associated-press-feed",
    ThisBuild / scalaVersion := scalaVersionSpec,
    libraryDependencies ++= Seq(
      ws,
      "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4",
      "software.amazon.awssdk" % "s3" % "2.19.32",
      "com.gu" %% "simple-configuration-ssm" % "1.5.7",
      "org.scalatest" %% "scalatest" % "3.2.15" % "test",
      "org.scalatestplus" %% "mockito-4-6" % "3.2.15.0" % "test",
    ),
    routesGenerator := InjectedRoutesGenerator,
    PlayKeys.playDefaultPort := 8855
  )

lazy val root = (project in file("."))
  .aggregate(associatedPressFeed)
  .settings(
    run / aggregate := true
  )
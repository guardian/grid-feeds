import com.typesafe.sbt.packager.archetypes.systemloader.ServerLoader.Systemd

lazy val scalaVersionSpec = "2.13.10"

lazy val associatedPressFeed =
  Project("associated-press-feed", file("associated-press-feed"))
    .enablePlugins(PlayScala, JDebPackaging, SystemdPlugin)
    .settings(
      name := "associated-press-feed",
      organization := "com.gu",
      maintainer := "Editorial Tools <digitalcms.dev@guardian.co.uk>",
      ThisBuild / scalaVersion := scalaVersionSpec,
      libraryDependencies ++= Seq(
        "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4",
        "software.amazon.awssdk" % "s3" % "2.19.27",
        ws
      ),
      routesGenerator := InjectedRoutesGenerator,
      PlayKeys.playDefaultPort := 8855
    )
    .settings(distributionSettings)

val distributionSettings = Seq(
  Universal / name := normalizedName.value,
  Debian / serverLoading := Some(Systemd),
)

lazy val root = (project in file("."))
  .aggregate(associatedPressFeed)
  .settings(
    run / aggregate := true
  )

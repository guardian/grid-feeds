import com.typesafe.sbt.packager.archetypes.systemloader.ServerLoader.Systemd

organization := "com.gu"
lazy val scalaVersionSpec = "2.13.10"

val distributionSettings = Seq(
  maintainer := "Editorial Tools <digitalcms.dev@guardian.co.uk>",
  Debian / name := normalizedName.value,
  Debian / serverLoading := Some(Systemd),
  Debian / serviceAutostart := true,
  debianPackageDependencies := Seq("openjdk-8-jre-headless"),
  Universal / javaOptions ++= Seq(
    "-Dpidfile.path=/dev/null",
    "-J-XX:+HeapDumpOnOutOfMemoryError",
    "-XX:InitialHeapSize=1G",
    "-XX:MaxHeapSize=1G"
  )
)

val riffraffSettings = Seq(
  riffRaffPackageName := s"editorial-tools:${name.value}",
  riffRaffManifestProjectName := riffRaffPackageName.value,
  riffRaffArtifactResources := Seq(
    (associatedPressFeed / Debian / packageBin).value -> s"${(associatedPressFeed / name).value}/${(associatedPressFeed / name).value}.deb",
    baseDirectory.value / "associated-press/cdk/cdk.out/AssociatedPressFeed-CODE.template.json" -> s"${(associatedPressFeed / name).value}/cloudformation/AssociatedPressFeed-CODE.template.json",
    baseDirectory.value / "associated-press/cdk/cdk.out/AssociatedPressFeed-PROD.template.json" -> s"${(associatedPressFeed / name).value}/cloudformation/AssociatedPressFeed-PROD.template.json",
    baseDirectory.value / "riff-raff.yaml" -> "riff-raff.yaml"
  )
)

lazy val associatedPressFeed =
  Project("associated-press-feed", file("associated-press"))
    .enablePlugins(PlayScala, JDebPackaging, SystemdPlugin)
    .settings(
      name := "associated-press-feed",
      ThisBuild / scalaVersion := scalaVersionSpec,
      libraryDependencies ++= Seq(
        ws,
        "software.amazon.awssdk" % "s3" % "2.20.27",
        "software.amazon.awssdk" % "dynamodb" % "2.20.27",
        "com.gu" %% "simple-configuration-ssm" % "1.5.7",
        "org.scalatest" %% "scalatest" % "3.2.15" % "test"
      ),
      routesGenerator := InjectedRoutesGenerator,
      PlayKeys.playDefaultPort := 8855
    )
    .settings(distributionSettings)

lazy val root = Project("grid-feeds", file("."))
  .aggregate(associatedPressFeed)
  .enablePlugins(RiffRaffArtifact)
  .settings(riffraffSettings)

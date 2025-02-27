import com.typesafe.sbt.packager.archetypes.systemloader.ServerLoader.Systemd

organization := "com.gu"
lazy val scalaVersionSpec = "2.13.15"

/* normalise Debian package name */
val normalisePackageName =
  taskKey[Unit]("Rename debian package name to be normalised")

val distributionSettings = Seq(
  maintainer := "Editorial Tools <digitalcms.dev@guardian.co.uk>",
  Debian / name := normalizedName.value,
  Debian / serverLoading := Some(Systemd),
  Debian / serviceAutostart := true,
  debianPackageDependencies := Seq("java17-runtime"),
  Universal / javaOptions ++= Seq(
    "-Dpidfile.path=/dev/null",
    "-J-XX:+HeapDumpOnOutOfMemoryError",
    "-XX:InitialHeapSize=1G",
    "-XX:MaxHeapSize=1G"
  )
)

val awsSdkV2Version = "2.30.29"

lazy val associatedPressFeed =
  Project("associated-press-feed", file("associated-press"))
    .enablePlugins(PlayScala, JDebPackaging, SystemdPlugin)
    .settings(
      name := "associated-press-feed",
      ThisBuild / scalaVersion := scalaVersionSpec,
      libraryDependencies ++= Seq(
        ws,
        "software.amazon.awssdk" % "s3" % awsSdkV2Version,
        "software.amazon.awssdk" % "dynamodb" % awsSdkV2Version,
        "com.gu" %% "simple-configuration-ssm" % "2.0.0",
        "org.scalatest" %% "scalatest" % "3.2.19" % "test"
      ),
      routesGenerator := InjectedRoutesGenerator,
      PlayKeys.playDefaultPort := 8855,
      Debian / packageName := normalizedName.value,
      normalisePackageName := {
        val targetDirectory = baseDirectory.value / "target"
        val debFile = (targetDirectory ** "*.deb").get().head
        val newFile =
          file(debFile.getParent) / ((Debian / packageName).value + ".deb")

        IO.move(debFile, newFile)
      }
    )
    .settings(distributionSettings)

lazy val root = Project("grid-feeds", file("."))
  .aggregate(associatedPressFeed)

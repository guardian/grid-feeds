addSbtPlugin("org.playframework" % "sbt-plugin" % "3.0.8")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.5")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.11.4")

libraryDependencies += "org.vafer" % "jdeb" % "1.14" artifacts Artifact(
  "jdeb",
  "jar",
  "jar"
)

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always

addSbtPlugin("org.playframework" % "sbt-plugin" % "3.0.7")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.4")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.11.1")

libraryDependencies += "org.vafer" % "jdeb" % "1.12" artifacts Artifact(
  "jdeb",
  "jar",
  "jar"
)

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.19")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.0")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.9")
addSbtPlugin("com.gu" % "sbt-riffraff-artifact" % "1.1.18")


libraryDependencies += "org.vafer" % "jdeb" % "1.10" artifacts Artifact("jdeb", "jar", "jar")

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always

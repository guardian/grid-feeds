addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.19")

ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)
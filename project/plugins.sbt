logLevel := Level.Warn

scalaVersion := "2.10.3"

conflictWarning := ConflictWarning.disable

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Sonatype Release" at "https://oss.sonatype.org/content/repositories/releases"

resolvers += "Sonatype Snapshot" at "https://oss.sonatype.org/content/repositories/snapshots"


resolvers += "mirror" at "http://mirrors.ibiblio.org/pub/mirrors/maven2/"

resolvers += "tomax repository" at "http://maven.tmx.com/nexus"

resolvers += "ghost4j repository" at "http://maven.newcorp.com/nexus/content/repositories/ghost4j-releases/"

resolvers += "jai repository" at "http://dl.bintray.com/jai-imageio/maven/"

resolvers += "Scalaz Bintray Repo" at "https://dl.bintray.com/scalaz/releases"

resolvers += Classpaths.typesafeResolver

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" %% "sbt-plugin" % "2.3.9")

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.0.6")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.0")

// web plugins

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.0")

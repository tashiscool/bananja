import sbt.Keys._
import sbt._
import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.config.ConfigFactory
import com.typesafe.sbt.less.Import.LessKeys
import play.twirl.sbt.Import.TwirlKeys
import sbt._
import Keys._
import com.typesafe.sbt.web.Import._
import com.typesafe.sbt.digest.Import._
import com.typesafe.sbt.gzip.Import._
import com.typesafe.sbt.SbtNativePackager._
import play.Play.autoImport._

object ApplicationBuild extends Build {

  val appName         = "bananja"
  val appVersion      = "0.1"
  val appOrganization    =  "com.bananja"
  val playVersion = "2.3.3"
  val scalaLangVersion = "2.11.1"

  val appDependencies = Seq(
    "com.typesafe.play" %% "play-ws" % "2.3.3",
    "org.webjars" %% "webjars-play" % "2.3.0-3",
    "org.webjars" % "bootstrap" % "3.3.2",
    "com.github.nscala-time" %% "nscala-time" % "1.8.0",
    "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.0.akka23" exclude("org.scala-stm", "scala-stm_2.10.0"),
    "org.reactivemongo" %% "reactivemongo" % "0.10.5.0.akka23",
    "org.springframework.data" % "spring-data-mongodb" % "1.3.1.RELEASE" exclude("org.scala-stm", "scala-stm_2.10.0"),
    "org.codehaus.jackson" % "jackson-mapper-lgpl" % "1.9.13",
    "com.google.guava" % "guava" % "18.0"
  )

  lazy val dist = com.typesafe.sbt.SbtNativePackager.NativePackagerKeys.dist

  publish <<= (publish) dependsOn  dist

  publishLocal <<= (publishLocal) dependsOn dist

  val distHack = TaskKey[File]("dist-hack", "Hack to publish dist")

  artifact in distHack ~= { (art: Artifact) => art.copy(`type` = "zip", extension = "zip") }

  val distHackSettings = Seq[Setting[_]] (
    distHack <<= (target in Universal, normalizedName, version) map { (targetDir, id, version) =>
      val packageName = "%s-%s" format(id, version)
      targetDir / (packageName + ".zip")
    }) ++ Seq(addArtifact(artifact in distHack, distHack).settings: _*)

  lazy val s = Seq(
    organization := appOrganization,
    version := appVersion,
    scalaVersion := scalaLangVersion,
    incOptions := incOptions.value.withNameHashing(true),
    resolvers ++= Seq(
      "Local Maven Repository" at "file:///"+Path.userHome.absolutePath+"/.m2/repository",
      "Spy Repository" at "http://files.couchbase.com/maven2",
      "Sonatype Release" at "https://oss.sonatype.org/content/repositories/releases",
      "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
      "Mirror" at "http://mirrors.ibiblio.org/pub/mirrors/maven2/",
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases"
    ),
    includeFilter in (Assets,LessKeys.less) := "*.less",
    includeFilter in digest := "*.css" || "*.js",
    includeFilter in gzip := "*.html" || "*.js" || "*.css",
    libraryDependencies ++= appDependencies,
    publish <<= publish dependsOn dist,
    crossPaths := false,
    artifact in distHack ~= { (art: Artifact) => art.copy(`type` = "zip", extension = "zip") },
    artifact in publish ~= {
      (art: Artifact) => art.copy(`type` = "zip", extension = "zip")
    },
    publishMavenStyle := true,
    distHack <<= (target in Universal, normalizedName, version) map { (targetDir, id, version) =>
      val packageName = "%s-%s" format(id, version)
      targetDir / (packageName + ".zip")
    }
  ) ++ Seq(distHackSettings: _*)

  lazy val root = Project(appName,file("."),settings = s).enablePlugins(play.PlayScala)
}



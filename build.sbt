import CustomReleaseSteps._
import java.util.Calendar
import com.gilt.sbt.newrelic.NewRelic.autoImport._
import com.typesafe.sbt.packager.archetypes.TemplateWriter
import sbt.Keys._
import sbtrelease.ReleaseStateTransformations._


lazy val credentialsFile = Path.userHome / ".ivy2" / ".credentials"
lazy val artifactoryCredentials =
  if (credentialsFile.isFile) {
    Credentials(credentialsFile)
  } else {
    Credentials(
      "Artifactory Realm",
      "artifactory.bananja.net",
      System.getenv("ARTIFACTORY_USERNAME"),
      System.getenv("ARTIFACTORY_PASSWORD")
    )
  }

lazy val bananjaVersion = "0.200.0"
lazy val logbackClassicLoggingVersion = "1.1.3"
lazy val scalaGuiceVersion = "4.0.1"
lazy val scalaLoggingVersion = "3.1.0"
lazy val scalaTestPlusVersion = "1.4.0"

buildInfoKeys := Seq[BuildInfoKey](
  name,
  version,
  BuildInfoKey.action("buildTime")(Calendar.getInstance().getTime)
)
buildInfoPackage := "bananja.info"
releaseVersionBump := sbtrelease.Version.Bump.Minor
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runTest,
  setReleaseVersion,
  stageApp,
  buildDockerImage,
  tagDockerImage,
  commitReleaseVersion,
  tagRelease,
  setNextVersion,
  commitNextVersion
)

organization := "com.bananja"

name := """drunkr"""

//Maintain SNAPSHOT suffix explicitly while in pre-release mode and still working out of master
version := "0.1.0"

maintainer := "tkhan@bananja.com"

scalaVersion := "2.11.7"

ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

/**
  * Settings to publish zip artifact to Nexus
  */
//lazy val dist = com.typesafe.sbt.SbtNativePackager.NativePackagerKeys.dist

publish <<= publish dependsOn  dist

publishLocal <<= publishLocal dependsOn dist

publishMavenStyle := true

val distZip = TaskKey[File]("dist-zip", "Publish zip artifact")

artifact in distZip ~= { (art: Artifact) => art.copy(`type` = "zip", extension = "zip") }

val distZipSettings = Seq[Setting[_]] (
  distZip <<= (target in Universal, normalizedName, version) map { (targetDir, id, version) =>
    val packageName = "%s-%s" format(id, version)
    targetDir / (packageName + ".zip")
  }
) ++ Seq(addArtifact(artifact in distZip, distZip).settings: _*)

distZipSettings

val gitHeadFile = SettingKey[File]("git HEAD file")

gitHeadFile := baseDirectory.value / ".git" / "HEAD"

/*
* Add a qualifier to the version at the time of build. This is driven by the branch in git that is being built.
* The current branch is determined by inspecting the /.git/HEAD file
*
* For master: use the version as is
* For develop: add -SNAPSHOT to the version
* For release branch: add -RC<timestamp> to the version
* For any other branch: add -SNAPSHOT to the version
*/
version <<= (version, gitHeadFile, sLog) {
  (v: String, gitHeadFile: File, log: Logger) =>
    //read   .git/HEAD to find the current branch
    val headRef = scala.util.Properties.envOrNone("GIT_BRANCH") match {
      case Some(branch) =>
        log.info(s"found branch $branch from GIT_BRANCH environment variable")
        branch
      case None =>
        log.info(s"GIT_BRANCH environment variable no set, checking .git/HEAD")
        IO.readLines(gitHeadFile).headOption.getOrElse {
          log.info(".git/HEAD not found either, defaulting to develop")
          "develop"
        }
    }

    log.info(s"headRef set to $headRef")

    if(headRef.contains("master")) {
      v
    } else if(headRef.contains("release")) {
      s"$v-RC${System.currentTimeMillis()}"
    } else {
      s"$v-SNAPSHOT"
    }
}

/**
  * Add scripts/start.sh to the bin directory in the packaged output
  */
mappings in Universal += {
  baseDirectory.value / "scripts" / "start.sh" -> "bin/start.sh"
}

/**
  *   For docker builds, specify the base Docker Image to use. Our base image here just has JDK7
  *
  *   See <a href="http://www.scala-sbt.org/sbt-native-packager/">sbt-native-packager</a> for more info
  *   on the SBT Native Packager plugin used for building the application as a Docker File
  */
  dockerBaseImage in Docker := "java:8-jdk"

/**
  * For docker builds, expose port 9000 from the docker container. This will map port 9000 on the docker host
  * to port 9000 in the running container
  *
  * See <a href="http://www.scala-sbt.org/sbt-native-packager/">sbt-native-packager</a> for more info
  *   on the SBT Native Packager plugin used for building the application as a Docker File
  */
dockerExposedPorts in Docker := Seq(9000)

lazy val root = (project in file(".")).enablePlugins(PlayScala)


resolvers ++= Seq(
  "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/",
  "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"
)

publishTo <<= version {
  (v: String) =>
    val repo = "https://artifactory.bananja.net/artifactory/"
    if (v.trim.endsWith("SNAPSHOT"))
      Some("Internal Snapshot Repository" at repo + "bananja-snapshots")
    else
      Some("Internal Release Respository" at repo + "bananja-releases")
}

credentials += artifactoryCredentials

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test,
  "com.typesafe.scala-logging"%%  "scala-logging"               % scalaLoggingVersion,
  "org.scalatestplus"         %%  "play"                        % scalaTestPlusVersion % "test",
  "com.typesafe.play"         %%  "play-slick"                  % "1.1.1",
  "com.softwaremill.macwire"  %%  "macros"                      % "2.2.0"             % "provided",
  "com.softwaremill.macwire"  %%  "util"                        % "2.2.0",
  "com.softwaremill.macwire"  %%  "proxy"                       % "2.2.0",
  "org.scalamock"             %%  "scalamock-specs2-support"    % "3.2.2"             % "test",
  "org.specs2"                %%  "specs2-core"                 % "2.4.17"            % "test",
  "org.specs2"                %%  "specs2-junit"                % "2.4.17"            % "test",
  "org.postgresql"            %  "postgresql"                  % "9.4-1201-jdbc41",
  "com.typesafe.slick"        %%  "slick"                       % "3.1.1"

)


/**
  *  Add New Relic instrumentation if the environment variable NR_ENABLED is set to true
  *
  *  See <a href="https://github.com/gilt/sbt-newrelic">sbt-newrelic</a> for more info on the
  *  sbt-newrelic plugin
  *
  * @return Sequence of settings for the sbt-newrelic plugin
  */
def newrelicSettings: Seq[Setting[_]] = {
  try {
    if(System.getProperty("newrelic.enabled","false").toBoolean) {
      NewRelic.packagerSettings ++ Seq(
        newrelicVersion := "3.23.0",
        newrelicLicenseKey := Some(""),
        newrelicCustomTracing := true,
        newrelicIncludeApi := true,
        newrelicAppName := "drunkr"
      )
    } else {
      Seq.empty
    }
  } catch {
    case e: Exception => Seq.empty
  }
}

newrelicSettings



/**
  *
  * Helper to make  [[sys.process.Process]] commands work on Windows
  *
  * @param command Command to run
  * @return Command that will work on Windows
  */
def panderToWindows(command: Seq[String]): Seq[String] = {
  sys.props("os.name").toLowerCase match {
    case x if x contains "windows" => Seq("cmd", "/C") ++ command
    case _ => command
  }
}

/** Add build information to configuration
  *
  */
lazy val generateBuildInfo = TaskKey[Unit]("generate-build-info")

generateBuildInfo := {

  val source = (baseDirectory.value / "conf" / "build.conf.template").toURL
  val replacements: Seq[(String,String)] = Seq(
    "BUILD_TAG" -> scala.util.Properties.envOrElse("BUILD_TAG","Undefined"),
    "BUILD_COMMIT" -> scala.util.Properties.envOrElse("GIT_COMMIT","Undefined"),
    "APP_VERSION" -> version.value
  )
  val fileContents = TemplateWriter.generateScript(source, replacements)
  sLog.value.debug(s"Source for build conf at ${source.toString}")
  val outFile = baseDirectory.value / "conf" / "build.conf"
  IO.write(outFile, fileContents)
}


/**
  * Key to determine whether this build is happening on Jenkins
  *
  * Default to false
  *
  * If not building on Jenkins, then skip the client build/test
  */
lazy val jenkinsBuild = SettingKey[Boolean]("jenkins-build")

jenkinsBuild := {
  System.getProperty("jenkins.build","false").toBoolean
}

/**
  *  Task Key for task that will build ReactJS client
  */
lazy val clientBuild = TaskKey[Unit]("Build client with webpack")


/**
  *  Task key for task that will run ReactJS client unit tests
  */
lazy val clientTest = TaskKey[Unit]("Run JS unit tests")

val npmInstallCommand = Seq("npm", "install")
val npmUpdateCommand = Seq("npm", "update")
val npmTestCommand = Seq("npm", "test")
val webpackCommand = Seq("webpack", "--bail")

/**
  *  Task for building ReactJS client
  *
  *  Only runs the actual NPM install/update if build is on Jenkins
  *
  *  This task will run the "npm install" command followed by "webpack --bail"  in the /client directory. This will
  *  ONLY happen if the /client/node_modules directory is not present.
  *
  *  Should fail if either command returns a non-zero exit code
  */
clientBuild := {
  if(jenkinsBuild.value) {
    val clientDir = baseDirectory.value / "client"
    val doInstall = !IO.listFiles(clientDir).exists(_.getPath.contains("node_modules"))

    val npmInstall = sys.process.Process(panderToWindows(npmInstallCommand), clientDir)
    val npmUpdate = sys.process.Process(panderToWindows(npmUpdateCommand), clientDir)
    val webpack = sys.process.Process(panderToWindows(webpackCommand), clientDir)
    val command = if(doInstall) {
      npmInstall #&& webpack
    } else {
      npmUpdate #&& webpack
    }
    if((command !) == 0) sLog.value.debug("client build successful") else throw new Exception("Client build failed")
  } else {
    sLog.value.debug("Not on Jenkins, skipping client build")
  }

}

/**
  *  Task for running ReactJS client unit tests
  *
  *  Only runs the actual NPM install/update if build is on Jenkins
  *
  *  This taks will run the {{{npm install}}} command followed by the {{{webpack --bail}}} command in the /client/test directory
  *
  *  Should fail if either command returns a non-zero exit code
  */
clientTest := {
  if(jenkinsBuild.value) {
    val testDir = baseDirectory.value / "client" / "test"
    val doInstall = !IO.listFiles(testDir).exists(_.getPath.contains("node_modules"))

    val npmInstall = sys.process.Process(panderToWindows(npmInstallCommand), testDir)
    val npmUpdate = sys.process.Process(panderToWindows(npmUpdateCommand), testDir)
    val npmTest = sys.process.Process(panderToWindows(npmTestCommand), testDir)
    val webpack = sys.process.Process(panderToWindows(webpackCommand), testDir)
    val command = if(doInstall) {
      npmInstall #&& webpack #&& npmTest
    } else {
      npmUpdate #&& webpack #&& npmTest
    }
    if((command !) == 0) sLog.value.debug("client tests successful") else throw new Exception("Client build failed: Test failures")
  } else {
    sLog.value.debug("Not on jenkings. Skipping client tests")
  }

}

/* Add the clientBuild and generateBuildInfo tasks to the compile phase of the build */
compile <<= (compile in Compile) dependsOn (clientBuild, generateBuildInfo)

/* Add the clientTest task to the test phase of the build */
test <<= (test in Test) dependsOn clientTest


testOptions in Test += Tests.Argument( "-Dlogger.resource=test-logger.xml")
(unmanagedResourceDirectories in Compile) += (baseDirectory.value / "conf" / "xsd")

testOptions in Test += Tests.Argument( "-Dlogger.resource=test-logger.xml")

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

publishArtifact in (Compile, packageDoc) := false

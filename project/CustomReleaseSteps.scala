import com.typesafe.sbt.packager.Keys._
import sbt.Keys.thisProjectRef
import sbt.{Global, Project, State}
import sbtrelease.ReleasePlugin.autoImport.{ReleaseStep, releaseTagName}
import sbtrelease.Utilities._

import scala.sys.process.stringSeqToProcess

object CustomReleaseSteps {

  lazy val buildDockerImage: ReleaseStep = { st: State =>
    val releaseTag = getReleaseTag(st)
    if (Seq("docker", "build", "-t", s"$dockerRepo:latest", ".").! != 0) st.fail else st
  }
  lazy val tagDockerImage: ReleaseStep = { st: State =>
    val releaseTag = getReleaseTag(st)
    if (Seq("docker", "tag", s"$dockerRepo:latest", s"$dockerRepo:$releaseTag").! != 0) st.fail else st
  }
  lazy val stageApp: ReleaseStep = ReleaseStep(
    action = { st: State =>
      val extracted = Project.extract(st)
      val ref = extracted.get(thisProjectRef)
      extracted.runAggregated(stage in Global in ref, st)
    },
    enableCrossBuild = true
  )
  val dockerRepo = "docker.bananja.com/drunkr"

  private def getReleaseTag(st: State): String = {
    st.extract.runTask(releaseTagName, st)._2
  }

}

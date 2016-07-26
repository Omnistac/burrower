import sbt._
import Keys._
import com.typesafe.sbt.packager.archetypes.JavaServerAppPackaging
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport._
import com.typesafe.sbt.packager.docker.{DockerPlugin, _}
import com.typesafe.sbt.packager.linux.LinuxPlugin.autoImport._
import sbt.dsl._

object BurrowerBuild extends Build {

  import Dependencies._
  import BuildSettings._

  override lazy val settings = super.settings :+ {
    shellPrompt := { s => Project.extract(s).currentProject.id + " > " }
  }

  lazy val project = Project("burrower", file("."))
    .settings(buildSettings: _*)
    .settings(
    libraryDependencies ++= Seq(
      Libraries.playJson,
      Libraries.scalaLogging,
      Libraries.logbackClassic,
      Libraries.scalaJ,
      Libraries.influxDb
    )
  )
    .settings(scalaVersion := "2.11.7")

  mainClass in Compile :=Some("com.github.splee.burrower.OffsetMonitor")

  enablePlugins(JavaServerAppPackaging)

  enablePlugins(DockerPlugin)

  mappings in Universal <++= (packageBin in Compile, sourceDirectory) map { (_, src) =>
    packageMapping(
      (src / "main" / "resources") -> "conf"
    ).withContents().mappings.map {
      case (f, p) => (f.asInstanceOf[java.io.File], p)
    }.toSeq
  }

  dockerRepository := Some("omnistac-docker-local.jfrog.io/omnistac")

  dockerBaseImage := "frolvlad/alpine-oraclejdk8"

  dockerCommands := dockerCommands.value.flatMap{   case cmd@Cmd("FROM",_) => List(cmd, Cmd("RUN", "apk update && apk add bash"))   case other => List(other) }

  dockerUpdateLatest := true

  version in Docker := sys.props.getOrElse("tag", default = version.value)

}

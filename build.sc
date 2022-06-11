import mill._, scalalib._, scalafmt.ScalafmtModule
import $ivy.`com.lihaoyi::mill-contrib-docker:$MILL_VERSION`
import contrib.docker.DockerModule

object sagittarius extends BaseModule with DockerModule {
  def scalaVersion = "3.1.2"
  def ivyDeps = Agg(
    ivy"org.bouncycastle:bcprov-jdk15to18:1.71",
    ivy"dev.zio::zio:2.0.0-RC6",
    ivy"dev.zio::zio-streams:2.0.0-RC6"
  )
  def scalacOptions = Seq("-source:future")
  object docker extends DockerConfig {
    def tags = List("mark.hammons.fr/sagittarius")

    def baseImage = "eclipse-temurin:latest"
    def exposedPorts= Seq(1965, 1965)
    def executable = "podman"
  }
}

trait BaseModule extends ScalaModule with ScalafmtModule {
  def scalaVersion = "3.1.2"

  def scalacOptions = Seq(
    "-source:future", "-Ysafe-init"
  )
}


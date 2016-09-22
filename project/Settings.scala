package showcase

import sbt._
import sbt.Keys._

object Settings extends AutoPlugin{

  override def trigger = allRequirements
  override def requires = plugins.JvmPlugin

  override lazy val projectSettings = Seq(
    organization := "com.github.dpfeiffer",
    scalaVersion := "2.11.8",
    scalacOptions ++= Seq(
      "-encoding", "UTF-8",
      "-feature",
      "-unchecked",
      "-deprecation",
      //"-Xfatal-warnings",
      "-Xlint",
      "-Yno-adapted-args",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Xfuture"
    )
  )

}
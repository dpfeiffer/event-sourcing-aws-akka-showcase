import sbt.Keys._

lazy val `event-sourcing-aws-akka-showcase` = (project in file(".")).aggregate(`time-entry-api`, `email-service`, events)

lazy val `time-entry-api` = (project in file("time-entry-api"))
  .enablePlugins(PlayScala)
  .dependsOn(events)
  .settings(
    scalaVersion := "2.11.8",
    name := "time-entry-api",
    libraryDependencies ++= Seq(
      "com.typesafe.akka"          %% "akka-persistence"                    % "2.4.10",
      "com.github.scullxbones"     %% "akka-persistence-mongo-rxmongo"      % "1.3.0",
      "com.typesafe.akka"          %% "akka-persistence-query-experimental" % "2.4.10",
      "org.typelevel"              %% "cats-core"                           % "0.7.2",
      "org.reactivemongo"          %% "reactivemongo"                       % "0.11.9",
      "com.typesafe.scala-logging" %% "scala-logging"                       % "3.5.0",
      "net.codingwell"             %% "scala-guice"                         % "4.1.0",
      ws
    )
  )

lazy val `email-service` = (project in file("email-service"))
  .dependsOn(events)
  .settings(
    scalaVersion := "2.11.8",
    name := "email-service",
    libraryDependencies ++= Seq(
      "com.amazonaws"     % "aws-java-sdk-sqs" % "1.11.29",
      "com.typesafe.akka" %% "akka-stream"     % "2.4.10",
      "net.ceedubs"       %% "ficus"           % "1.1.2",
      "org.typelevel"     %% "cats-core"       % "0.7.2",
      "com.typesafe.play" %% "play-json"       % "2.5.8"
    )
  )

lazy val events = (project in file("events")).settings(
  scalaVersion := "2.11.8",
  libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play-json" % "2.5.8",
    "joda-time"         % "joda-time"  % "2.9.4"
  )
)

scalaVersion := "2.11.8"

lazy val `event-sourcing-aws-akka-showcase` = (project in file(".")).aggregate(local)
lazy val local                              = project

lazy val `email-service` = (project in file("email-service"))
  .dependsOn(events)
  .settings(
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
  libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play-json" % "2.5.8",
    "joda-time"         % "joda-time"  % "2.9.4"
  )
)

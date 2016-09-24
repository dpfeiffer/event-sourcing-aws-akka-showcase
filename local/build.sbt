lazy val local = (project in file(".")).enablePlugins(PlayScala)
name := "local"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka"      % "akka-persistence_2.11"               % "2.4.10",
  "com.github.scullxbones" % "akka-persistence-mongo-rxmongo_2.11" % "1.3.0",
  "com.typesafe.akka" % "akka-persistence-query-experimental_2.11" % "2.4.10",
  "org.typelevel"          % "cats-core_2.11"                      % "0.7.2",
  "org.reactivemongo"      % "reactivemongo_2.11"                  % "0.11.9",
  ws
)

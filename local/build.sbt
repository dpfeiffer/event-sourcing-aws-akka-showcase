
lazy val local = (project in file(".")).enablePlugins(PlayScala)
name := "local"

libraryDependencies ++= Seq(
  ws
)
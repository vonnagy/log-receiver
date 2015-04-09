import NativePackagerKeys._

packageArchetype.java_application

name := "log-receiver"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  "com.github.vonnagy" % "service-container_2.10" % "1.0.0"
)

// set the main class for packaging the main jar
// 'run' will still auto-detect and prompt
// change Compile to Test to set it for the test jar
mainClass in (Compile, packageBin) := Some("logreceiver.Service")

// set the main class for the main 'run' task
// change Compile to Test to set it for 'test:run'
mainClass in (Compile, run) := Some("logreceiver.Service")
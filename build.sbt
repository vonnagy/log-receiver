import NativePackagerKeys._

packageArchetype.java_application

name := "log-receiver"

version := "1.0"

scalaVersion := "2.10.4"

val AKKA_VERSION = "2.3.8"
val SPRAY_VERSION = "1.3.1"

bintrayResolverSettings

libraryDependencies ++= Seq(
  "com.github.vonnagy"    % "service-container_2.10"  % "1.0.0",
  "com.amazonaws"         % "amazon-kinesis-client"   % "1.2.1",
  "io.github.cloudify"    %% "scalazon"               % "0.11",
  "com.typesafe.akka"     %% "akka-testkit"           % AKKA_VERSION    % "test",
  "io.spray"              % "spray-testkit"           % SPRAY_VERSION   % "test",
  "junit"                 % "junit"                   % "4.12"          % "test",
  "org.specs2"            %% "specs2-core"            % "2.4.15"        % "test",
  "org.scalamock"         %% "scalamock-specs2-support" % "3.2.1" % "test" exclude("org.specs2", "specs2")
)

// set the main class for packaging the main jar
// 'run' will still auto-detect and prompt
// change Compile to Test to set it for the test jar
mainClass in (Compile, packageBin) := Some("logreceiver.Service")

// set the main class for the main 'run' task
// change Compile to Test to set it for 'test:run'
mainClass in (Compile, run) := Some("logreceiver.Service")
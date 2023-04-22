ThisBuild / version := "1.0.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

val akkaHttpV     = "10.2.9"
val circe         = "0.14.1"
val akkaHttpCirce = "1.39.2"
val akkaV         = "2.6.9"
val quillV        = "3.12.0"
val scalaLoggingV = "3.9.2"
val logbackV      = "1.2.3"
val scalaTestV    = "3.1.4"

libraryDependencies ++= Seq(
  "ch.qos.logback"              % "logback-classic"          % logbackV,
  "com.typesafe.akka"          %% "akka-http"                % akkaHttpV,
  "com.typesafe.akka"          %% "akka-stream"              % akkaV,
  "com.typesafe.akka"          %% "akka-slf4j"               % akkaV,
  "de.heikoseeberger"          %% "akka-http-circe"          % akkaHttpCirce,
  "com.typesafe.akka"          %% "akka-stream"              % akkaV,
  "io.circe"                   %% "circe-generic"            % circe,
  "io.getquill"                %% "quill-async-postgres"     % quillV,
  "com.typesafe.scala-logging" %% "scala-logging"            % scalaLoggingV,
  "org.scalatest"              %% "scalatest"                % scalaTestV % Test,
  "com.typesafe.akka"          %% "akka-http-testkit"        % akkaHttpV  % Test,
  "com.typesafe.akka"          %% "akka-stream-testkit"      % akkaV      % Test,
  "com.typesafe.akka"          %% "akka-actor-testkit-typed" % akkaV      % Test
)

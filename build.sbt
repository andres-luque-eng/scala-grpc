name := "scala-grpc"
version := "0.1.0"
scalaVersion := "3.10.0"

lazy val root = (project in file("."))
  .enablePlugins(PekkoGrpcPlugin)
  .settings(
    Compile / run / mainClass := Some("com.example.grpc.Server"),
    Compile / run / fork := true,
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-actor-typed" % "1.1.2",
      "org.apache.pekko" %% "pekko-stream"      % "1.1.2",
      "org.apache.pekko" %% "pekko-http"        % "1.1.0",
      // SLF4J implementation: required to see Pekko and server log output instead of defaulting to NOP logger
      "ch.qos.logback"    % "logback-classic"   % "1.5.12"
    )
  )

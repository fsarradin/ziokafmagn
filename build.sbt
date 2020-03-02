name := "zkafka"

version := "0.1"

scalaVersion := "2.13.1"

libraryDependencies ++= Seq(
  "dev.zio"        %% "zio"         % "1.0.0-RC17",
  "dev.zio"        %% "zio-streams" % "1.0.0-RC17",
  "dev.zio"        %% "zio-kafka"   % "0.5.0",
  "com.propensive" %% "magnolia"    % "0.12.6"
)

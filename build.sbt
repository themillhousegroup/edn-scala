name := "edn-scala"

organization := "com.themillhousegroup"

version := "1.0"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq (
  "us.bpsm"               %   "edn-java"              % "0.4.4",
  "org.specs2"            %%  "specs2"                % "2.3.12" % "test",
  "org.mockito"           %   "mockito-all"           % "1.9.0" % "test"
)

jacoco.settings

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

publishTo := Some("Cloudbees releases" at "https://repository-themillhousegroup.forge.cloudbees.com/"+ "release")


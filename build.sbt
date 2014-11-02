name := "edn-scala"

organization := "com.themillhousegroup"


/*  1.x: Basic functionality
    2.x: Read-into case-class
    3.x: Typesafe Config adapter
*/
version := "4.0.0"

scalaVersion := "2.11.2"

libraryDependencies ++= Seq (
  "us.bpsm"               %   "edn-java"              % "0.4.4",
  "com.google.guava"      %   "guava"                 % "18.0",
  "ch.qos.logback"        %  "logback-classic"        % "1.1.2",
  "com.typesafe"          %  "config"                 % "1.2.1",
  "com.themillhousegroup" %% "sausagefactory"         % "0.2.0",
  "org.specs2"            %%  "specs2"                % "2.3.12" % "test",
  "org.mockito"           %   "mockito-all"           % "1.9.0" % "test"
)

resolvers ++= Seq(
    "millhouse-releases" at "http://repository-themillhousegroup.forge.cloudbees.com/release"
)

jacoco.settings

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

seq(bintraySettings:_*)

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

scalariformSettings


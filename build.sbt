name := "twitter-survey"

organization := "com.github.javifdev"

version := "1.0.0"

scalaVersion := "2.11.6"

resolvers += "Scalaz Bintray Repo" at "https://dl.bintray.com/scalaz/releases"

//Define dependencies. These ones are only required for Test and Integration Test scopes.
libraryDependencies ++= Seq(
	"org.scalaz.stream" %% "scalaz-stream" % "0.7a",
    "org.twitter4j" % "twitter4j-core" % "4.0.3",
    "com.typesafe" % "config" % "1.2.1")

scalacOptions ++= List("-feature","-deprecation", "-unchecked", "-Xlint")

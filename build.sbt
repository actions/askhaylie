
import sbt._

name := "askhaylie"

version := "0.0.1-SNAPSHOT"

organization := "sergey"

scalaVersion := "2.11.6"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

libraryDependencies ++= Seq(
  "com.amazon.alexa" % "alexa-skills-kit" % "1.2"
)

resolvers ++= Seq(
  "Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/",
  "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
)

javaOptions in run += "-Xmx8G"

scalacOptions ++= Seq("-deprecation", "-unchecked")

//test in assembly := {}

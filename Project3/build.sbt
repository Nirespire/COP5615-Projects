name := "COP5615 Project 3"
 
version := "1.0"
 
scalaVersion := "2.10.4"
 
resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.2.0"

libraryDependencies += "com.google.guava" % "guava" % "17.0"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"

scalacOptions += "-deprecation"
name := "COP5615 Project 1"
 
version := "1.0"
 
scalaVersion := "2.10.4"
 
resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
 
libraryDependencies += "com.typesafe.akka" % "akka-actor_2.10" % "2.3.12"

libraryDependencies += "com.typesafe.akka" %% "akka-remote" % "2.3.12"

scalacOptions += "-deprecation"
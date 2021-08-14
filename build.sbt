name := "archF"

version := "0.1"

scalaVersion := "2.13.6"

libraryDependencies += "org.typelevel" %% "cats-effect" % "3.2.1"

libraryDependencies +="tfox" %% "immersivecollections"% "0.1"

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
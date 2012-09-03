// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository 
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("play" % "sbt-plugin" % "2.1-08212012")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.1.0-TYPESAFE")
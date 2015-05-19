resolvers ++= Seq("Banno Snapshots Repo" at "http://nexus.banno.com/nexus/content/repositories/snapshots",
                  "Banno Releases Repo" at "http://nexus.banno.com/nexus/content/repositories/releases",
                  "Banno External Repo" at "http://nexus.banno.com/nexus/content/groups/external/")

addSbtPlugin("com.banno" % "banno-sbt-plugin" % "5")

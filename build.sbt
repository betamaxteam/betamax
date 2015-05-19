import com.banno._
import sbtassembly.Plugin._
import AssemblyKeys._

lazy val root = (project in file(".")).
  settings(
    name := "betamax",
    organization := "co.freeside",
    crossPaths := false // Because Groovy
  )

lazy val gradle = taskKey[Unit]("Execute the gradle build script")

packageBin in Compile := file(s"build/libs/betamax-${version.value}.jar")

gradle := {
  "./gradlew clean test assemble" !
}

name := "delivery-lambda"

version := "0.1"

scalaVersion := "2.12.11"

val monixVersion = "3.0.0-RC2"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.2.0",
  "io.monix" %% "monix" % "3.2.2",
  "io.monix" %% "monix-cats" % "2.3.3",
  "com.softwaremill.sttp.client" %% "async-http-client-backend-monix" % "2.2.0",
  "com.softwaremill.sttp.client" %% "circe" % "2.2.0",
  "io.circe" %% "circe-generic" % "0.12.1",
  "com.amazonaws" % "aws-java-sdk-s3" % "1.11.787",
  "com.typesafe" % "config" % "1.3.4",
)


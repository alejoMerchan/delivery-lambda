name := "delivery-lambda"
version := "0.1"
scalaVersion := "2.12.11"

lazy val Versions = new {
  val awsLambdaJavaCore = "1.2.0"
  val monix             = "3.2.2"
  val monixCats         = "2.3.3"
  val sttpClient        = "2.2.0"
  val circe             = "0.12.1"
  val awsS3             = "1.11.787"
  val typesafe          = "1.3.4"
}

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x                             => MergeStrategy.first
}

libraryDependencies ++= Seq(
  "com.amazonaws"                % "aws-lambda-java-core"             % Versions.awsLambdaJavaCore,
  "io.monix"                     %% "monix"                           % Versions.monix,
  "io.monix"                     %% "monix-cats"                      % Versions.monixCats,
  "com.softwaremill.sttp.client" %% "async-http-client-backend-monix" % Versions.sttpClient,
  "com.softwaremill.sttp.client" %% "circe"                           % Versions.sttpClient,
  "io.circe"                     %% "circe-generic"                   % Versions.circe,
  "com.amazonaws"                % "aws-java-sdk-s3"                  % Versions.awsS3,
  "com.typesafe"                 % "config"                           % Versions.typesafe,
)

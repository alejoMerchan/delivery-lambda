package com.app.sourcing

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.amazonaws.services.s3.model.PutObjectResult
import com.app.sourcing.ConfigVars._
import com.app.sourcing.client._



class SourcingHandler extends RequestHandler[Unit, Unit] {

  val gitHubClient: GitHubClient = GitHubClient()
  val s3SourcingClient: SourcingS3Client = SourcingS3Client(regionName)

  def handleRequest(input: Unit, context: Context): Unit = {
    val lambdaLogger = context.getLogger
    lambdaLogger.log("--- starting process ---")
    process()
    lambdaLogger.log("--- process finished ---")
  }

  def process(): Unit = {

    val dataUsers: List[Option[GitHubFullUser]] =
      getDataUsers(ConfigVars.usersInitVal, ConfigVars.usersMaxVal)
    val dataRepositories: List[Option[GitHubFullRepo]] = getDataRepositories

    val s3SaveResult = storageData(
      dataUsers,
      dataRepositories,
      bucketName,
      usersFilename,
      reposFilename,
      usersHeaderLine,
      reposHeaderLine)

    println(s"--- S3 save result $s3SaveResult")
  }

  def getDataUsers(initVal: Long, maxVal: Long): List[Option[GitHubFullUser]] = {
    gitHubClient.getUser(gitHubClient.getUsers(initVal, maxVal).map(_.login))
  }

  def getDataRepositories: List[Option[GitHubFullRepo]] = {
    gitHubClient.getFullRepositories(gitHubClient.getRepositories)
  }

  def storageData(
      dataUsers: List[Option[Any]],
      dataRepos: List[Option[Any]],
      bucketName: String,
      usersFilename: String,
      reposFilename: String,
      usersHeaderLine: String,
      reposHeaderLine: String): List[PutObjectResult] = {
    println(s"--- storageData - total users: ${dataUsers.size}")
    println(s"--- storageData - total repos: ${dataRepos.size}")
    val r1 = SourcingS3ClientRequest(
      S3Object(Some(usersHeaderLine) :: dataUsers, bucketName, usersFilename))
    val r2 = SourcingS3ClientRequest(
      S3Object(Some(reposHeaderLine) :: dataRepos, bucketName, reposFilename))
    s3SourcingClient.saveFileCSV(List(r1, r2)).unsafeRunSync()
  }
}

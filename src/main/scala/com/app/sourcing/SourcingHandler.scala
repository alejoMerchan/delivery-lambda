package com.app.sourcing

import com.amazonaws.services.lambda.runtime.{Context, LambdaLogger, RequestHandler}
import com.amazonaws.services.s3.model.PutObjectResult
import com.app.sourcing.client._
import com.app.sourcing.entity.{GitHubFullRepo, GitHubFullUser}
import com.app.sourcing.service.conf.BucketConf._
import com.app.sourcing.service.conf.UserConf._
import com.app.sourcing.service.conf.ReposConf._

class SourcingHandler extends RequestHandler[Unit, Unit] {

  val gitHubClient: GitHubClient = GitHubClient()
  val s3SourcingClient: SourcingS3Client = SourcingS3Client(regionName)

  def handleRequest(input: Unit, context: Context): Unit = {
    val lambdaLogger = context.getLogger
    lambdaLogger.log("--- starting process ---")
    process(lambdaLogger)
    lambdaLogger.log("--- process finished ---")
  }

  def process(lambdaLogger: LambdaLogger): Unit = {

    val dataUsers: List[Option[GitHubFullUser]] =
      gitHubClient.getUser(gitHubClient.getUsers(usersMaxRequests).map(_.login))

    val dataRepositories: List[Option[GitHubFullRepo]] =
      gitHubClient.getFullRepositories(gitHubClient.getRepositories(reposMaxRequests))

    val s3SaveResult = storageData(
      lambdaLogger,
      dataUsers,
      dataRepositories,
      bucketName,
      usersFilename,
      reposFilename,
      usersHeaderLine,
      reposHeaderLine)

    lambdaLogger.log(s"--- S3 save result $s3SaveResult")
  }

  def storageData(
      lambdaLogger: LambdaLogger,
      dataUsers: List[Option[Any]],
      dataRepos: List[Option[Any]],
      bucketName: String,
      usersFilename: String,
      reposFilename: String,
      usersHeaderLine: String,
      reposHeaderLine: String): List[PutObjectResult] = {
    lambdaLogger.log(s"--- storageData - total users: ${dataUsers.size}")
    lambdaLogger.log(s"--- storageData - total repos: ${dataRepos.size}")
    val r1 = SourcingS3ClientRequest(
      S3Object(Some(usersHeaderLine) :: dataUsers, bucketName, usersFilename))
    val r2 = SourcingS3ClientRequest(
      S3Object(Some(reposHeaderLine) :: dataRepos, bucketName, reposFilename))
    s3SourcingClient.saveFileCSV(List(r1, r2)).unsafeRunSync()
  }
}

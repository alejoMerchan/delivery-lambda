package com.app.sourcing

import com.amazonaws.services.lambda.runtime.{Context, LambdaLogger, RequestHandler}
import com.amazonaws.services.s3.model.PutObjectResult
import com.app.sourcing.client._
import com.app.sourcing.entity.{GitHubFullRepo, GitHubFullUser}
import com.app.sourcing.service.conf.BucketConf._
import com.app.sourcing.service.conf.ReposConf._
import com.app.sourcing.service.conf.UserConf._
import com.app.sourcing.service.conf.{ReposConf, UserConf}

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

    /** Users using search endpoint. */
    if (usersMaxRequests > 0) {
      val dataUsers: List[Option[GitHubFullUser]] =
        gitHubClient.getUser(gitHubClient.getUsers(usersMaxRequests, UserConf.initVal).map(_.login))
      val s3UserResult =
        storageData(lambdaLogger, dataUsers, usersHeaderLine, usersFilename)
      lambdaLogger.log(s"--- S3 $usersFilename save result $s3UserResult")
    }

    /** Users using get endpoint. */
    if (reposMaxRequests > 0) {
      val dataRepositories: List[Option[GitHubFullRepo]] =
        gitHubClient.getFullRepositories(
          gitHubClient.getRepositories(reposMaxRequests, ReposConf.initVal))
      val s3ReposResult =
        storageData(lambdaLogger, dataRepositories, reposHeaderLine, reposFilename)
      lambdaLogger.log(s"--- S3 $reposFilename save result $s3ReposResult")
    }

    /** Repositories using get endpoint. */
    if (maxSearchRequests > 0) {
      val usersSearchData: List[Option[GitHubFullUser]] =
        gitHubClient.getUser(
          gitHubClient.searchUsers(maxSearchRequests, searchInitPage).map(_.login))
      val s3SearchResult =
        storageData(lambdaLogger, usersSearchData, usersHeaderLine, usersSearchFilename)
      lambdaLogger.log(s"--- S3 $usersSearchFilename save result $s3SearchResult")
    }
  }

  def storageData(
      lambdaLogger: LambdaLogger,
      data: List[Option[Any]],
      headerLine: String,
      fileName: String): List[PutObjectResult] = {
    lambdaLogger.log(s"--- storageData - $fileName total data: ${data.size}")
    val s3Req = SourcingS3ClientRequest(S3Object(Some(headerLine) :: data, bucketName, fileName))
    s3SourcingClient.saveFileCSV(List(s3Req)).unsafeRunSync()
  }
}

package com.app.sourcing

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.amazonaws.services.s3.model.PutObjectResult
import com.app.sourcing.ConfigVars._
import com.app.sourcing.client._
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.duration._

class SourcingHandler extends RequestHandler[Unit, Unit] {

  val gitHubClient: GitHubClient = GitHubClient()
  val s3SourcingClient: SourcingS3Client = SourcingS3Client(regionName)

  def handleRequest(input: Unit, context: Context): Unit = {
    val lambdaLogger = context.getLogger
    lambdaLogger.log("--- starting process ---")
    process().runSyncUnsafe(context.getRemainingTimeInMillis seconds)
    lambdaLogger.log("--- process finished ---")
  }

  def process(): Task[Unit] = {
    for {
      dataUsers <- getDataUsers
      dataRepositories <- getDataRepositories
      storage <- storageData(
        dataUsers,
        dataRepositories,
        bucketName,
        usersFilename,
        reposFilename,
        usersHeaderLine,
        reposHeaderLine)
      result = {
        storage.attempt.map {
          case Left(error) =>
            println(s"Problem in the repositories process. Error: ${error.getMessage}")
          case Right(_) =>
            println("Repositories process success")
        }
      }
    } yield result.unsafeRunSync()
  }

  def getDataUsers: TaskListOption[GitHubFullUser] = {
    gitHubClient.getUser(gitHubClient.getUsers.map(_.login))
  }

  def getDataRepositories: TaskListOption[GitHubFullRepo] = {
    gitHubClient.getFullRepositories(gitHubClient.getRepositories)
  }

  def storageData(
      dataUsers: List[Option[Any]],
      dataRepos: List[Option[Any]],
      bucketName: String,
      usersFilename: String,
      reposFilename: String,
      usersHeaderLine: String,
      reposHeaderLine: String): TaskIOList[PutObjectResult] = {
    Task {
      val r1 = SourcingS3ClientRequest(
        S3Object(Some(usersHeaderLine) :: dataUsers, bucketName, usersFilename))
      val r2 = SourcingS3ClientRequest(
        S3Object(Some(reposHeaderLine) :: dataRepos, bucketName, reposFilename))
      s3SourcingClient.saveFileCSV(List(r1, r2))
    }
  }
}

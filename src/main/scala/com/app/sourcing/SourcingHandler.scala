package com.app.sourcing

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.amazonaws.services.s3.model.PutObjectResult
import com.app.sourcing.ConfigVars.{bucketName, reposFilename, usersFilename}
import com.app.sourcing.client._
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.duration._

class SourcingHandler extends RequestHandler[Unit, Unit] {

  val gitHubClient: GitHubClient = GitHubClient()
  val s3SourcingClient: SourcingS3Client = SourcingS3Client()

  def handleRequest(input: Unit, context: Context): Unit = {
    val lambdaLogger = context.getLogger
    lambdaLogger.log("--- starting process ---")
    process().runSyncUnsafe(context.getRemainingTimeInMillis seconds)
    lambdaLogger.log("--- process finished ---")
  }

  def process(): Task[Unit] = {
    for {
      data <- getDataUsers
      dataRepositories <- getDataRepositories
      storage <- storageData(data, dataRepositories, bucketName, usersFilename, reposFilename)
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
      data: List[Option[Any]],
      data2: List[Option[Any]],
      bucketName: String,
      usersFilename: String,
      reposFilename: String): TaskIOList[PutObjectResult] = {
    Task {
      val r1 = SourcingS3ClientRequest(S3Object(data, bucketName, usersFilename))
      val r2 = SourcingS3ClientRequest(S3Object(data2, bucketName, reposFilename))
      s3SourcingClient.saveFileCSV(List(r1, r2))
    }
  }
}

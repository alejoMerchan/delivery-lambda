package com.app.sourcing.client

import java.util.regex.{Matcher, Pattern}

import com.app.sourcing.entity.{GitHubFullRepo, GitHubFullUser, GitHubUser}
import com.app.sourcing.service.{RepoService, UserService}
import com.app.sourcing.service.conf.AuthConf._
import com.app.sourcing.service.conf.ReposConf._
import com.app.sourcing.service.conf.ApiParamsConf._
import com.app.sourcing.service.conf.UserConf._
import io.circe.generic.auto._
import sttp.client._
import sttp.client.circe._

import scala.util.matching.Regex

object GitHubClient {

  def apply(): GitHubClient = {
    new GitHubClient()
  }
}

class GitHubClient() extends UserService with RepoService {

  implicit val backend = HttpURLConnectionBackend()

  def getUsers(maxRequests: Int): List[GitHubUser] = {
    getBatchUser(0, List.empty[GitHubUser], maxRequests)
  }

  def getRepositories(maxRequests: Int): List[GitHubFullRepo] = {
    getBatchRepository(0, List.empty[GitHubFullRepo], maxRequests)
  }

  def getUser(users: List[String]): List[Option[GitHubFullUser]] = {
    users.map { user =>
      val response = basicRequest
        .header(authHeader, s"$tokenType $token")
        .get(uri"$usersUri/$user")
        .response(asJson[GitHubFullUser])
        .send()
      response.body match {
        case Left(_) =>
          None
        case Right(value) =>
          Some(value)
      }
    }
  }

  def getFullRepositories(repositories: List[GitHubFullRepo]): List[Option[GitHubFullRepo]] = {
    repositories.map { repo =>
      val response = basicRequest
        .header(authHeader, s"$tokenType $token")
        .get(uri"${repo.languages_url}")
        .send()
      response.body match {
        case Left(_) =>
          None
        case Right(value) =>
          if (value.nonEmpty) {
            val languages = extractLanguages(value)
            Some(repo.copy(languages = Some(languages)))
          } else {
            Some(repo.copy(languages = None))
          }
      }
    }
  }

  def getBatchUser(
      init: Long,
      users: List[GitHubUser],
      maxRequests: Int): List[GitHubUser] = {
    println(s"------ getBachUser init: $init, maxRequests: $maxRequests")

    implicit val backend = HttpURLConnectionBackend()
    val request = basicRequest
      .header(authHeader, s"$tokenType $token")
      .get(uri"$usersUri?$pageUriParam=$init")
      .response(asJson[Seq[GitHubUser]])
    val response = request.send()

    if (response.isSuccess) {
      response.body match {
        case Left(error) =>
          println(s"-- getBatchUser body ERROR - ${error.getMessage}")
          users
        case Right(value) =>
          val nextBatch = nextSinceParam(
            response.headers.filter(_.name.equals(linkHeader)).head.value.split(";")(0))
          println(s"-- getBachUser next since: $nextBatch")
          val usersList = users ++ value
          if (maxRequests == 0 || ((maxRequests - 1) <= usersList.size)) users
          else getBatchUser(nextBatch, users ++ value, maxRequests - 1)
      }
    } else {
      println(s"-- getBachUser - response ERROR . result: $response")
      users
    }
  }

  def getBatchRepository(
      init: Long,
      repos: List[GitHubFullRepo],
      maxRequests: Int): List[GitHubFullRepo] = {
    println(s"------ getBatchRepository init: $init, maxRequests: $maxRequests")

    val request = basicRequest
      .header(authHeader, s"$tokenType $token")
      .get(uri"$reposUri?$pageUriParam=$init")
      .response(asJson[Seq[GitHubFullRepo]])
    val response = request.send()

    if (response.isSuccess) {
      response.body match {
        case Left(error) =>
          println(s"-- getBatchRepository body ERROR: ${error.getMessage}")
          repos
        case Right(value) =>
          val nextBatch = nextSinceParam(
            response.headers.filter(_.name.equals(linkHeader)).head.value.split(";")(0))
          val reposList = repos ++ value
          if (maxRequests == 0 || ((maxRequests - 1) <= reposList.size)) repos
          else getBatchRepository(nextBatch, repos ++ value, maxRequests - 1)
      }
    } else {
      println(s"-- getBatchRepository response ERROR: $response")
      repos
    }
  }

  private def searchLanguage(matcher: Matcher, languages: List[String]): List[String] = {
    if (matcher.find)
      searchLanguage(matcher, matcher.group(1) :: languages)
    else
      languages
  }

  private def extractLanguages(languages: String): List[String] = {
    val p = Pattern.compile("\"([^\"]*)\"");
    val m = p.matcher(languages);
    searchLanguage(m, List.empty[String])
  }

  /** Get uri for next default page.
    * Link header should be like:
    * <https://api.github.com/repositories?since=876> */
  private def nextSinceParam(linkHeader: String): Long = {
    println(s"--- linkHeader: $linkHeader")
    val pattern: Regex = """^<https://.+since=(\d+)>$""".r
    linkHeader
      .split(",")
      .toList
      .headOption
      .getOrElse(0L)
      .toString
      .split(";")
      .toList
      .headOption
      .getOrElse(0)
      .toString match {
      case pattern(x) => x.toLong
      case _          => 0L
    }
  }
}

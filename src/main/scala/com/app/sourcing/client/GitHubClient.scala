package com.app.sourcing.client

import java.util.regex.{Matcher, Pattern}

import com.app.sourcing.entity.{GitHubFullRepo, GitHubFullUser, GitHubUser, GitHubUserSearchResult}
import com.app.sourcing.service.conf.ApiParamsConf._
import com.app.sourcing.service.conf.AuthConf._
import com.app.sourcing.service.conf.ReposConf._
import com.app.sourcing.service.conf.UserConf._
import com.app.sourcing.service.{RepoService, UserService}
import io.circe.generic.auto._
import sttp.client._
import sttp.client.circe._
import sttp.model.Header

import scala.util.matching.Regex

object GitHubClient {

  def apply(): GitHubClient = {
    new GitHubClient()
  }
}

//noinspection ScalaStyle
class GitHubClient() extends UserService with RepoService {

  implicit val backend = HttpURLConnectionBackend()

  def getUsers(maxRequests: Int, initVal: Long): List[GitHubUser] = {
    println(s"-- getUsers - maxRequests: $maxRequests")
    getBatchUser(initVal, List.empty[GitHubUser], maxRequests)
  }

  def getRepositories(maxRequests: Int, initVal: Long): List[GitHubFullRepo] = {
    println(s"-- getRepositories - maxRequests: $maxRequests")
    getBatchRepository(initVal, List.empty[GitHubFullRepo], maxRequests)
  }

  def searchUsers(maxRequests: Int, initPage: Long): List[GitHubUser] = {
    println(s"-- searchBatchUser - maxRequests: $maxRequests")
    searchBatchUser(initPage, List.empty[GitHubUser], maxRequests)
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

  @scala.annotation.tailrec
  private def getBatchUser(
      init: Long,
      users: List[GitHubUser],
      maxRequests: Int): List[GitHubUser] = {
    println(s"-- getBatchUser - uri: $usersUri?$pageUriParam=$init")

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
          val nextBatch = nextPageParam(
            response.headers
              .find(_.name.equals(linkHeader))
              .getOrElse(Header("", ""))
              .value
              .split(";")(0),
            pageUriParam)
          val usersList = users ++ value
          if (maxRequests == 0 || ((maxRequests - 1) <= usersList.size)) users
          else getBatchUser(nextBatch, usersList, maxRequests - 1)
      }
    } else {
      println(s"-- getBachUser - response ERROR . result: $response")
      users
    }
  }

  @scala.annotation.tailrec
  private def getBatchRepository(
      init: Long,
      repos: List[GitHubFullRepo],
      maxRequests: Int): List[GitHubFullRepo] = {
    println(s"-- getBatchRepository - uri: $reposUri?$pageUriParam=$init")

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
          val nextBatch = nextPageParam(
            response.headers
              .find(_.name.equals(linkHeader))
              .getOrElse(Header("", ""))
              .value
              .split(";")(0),
            pageUriParam)
          val reposList = repos ++ value
          if (maxRequests == 0 || ((maxRequests - 1) <= reposList.size)) repos
          else getBatchRepository(nextBatch, reposList, maxRequests - 1)
      }
    } else {
      println(s"-- getBatchRepository response ERROR: $response")
      repos
    }
  }

  @scala.annotation.tailrec
  private def searchBatchUser(
      initPage: Long,
      users: List[GitHubUser],
      maxRequests: Int): List[GitHubUser] = {
    println(s"-- searchBatchUser - uri: $searchUri&$searchPageParam=$initPage")

    implicit val backend = HttpURLConnectionBackend()
    val request = basicRequest
      .header(authHeader, s"$tokenType $token")
      .get(uri"$searchUri&$searchPageParam=$initPage")
      .response(asJson[GitHubUserSearchResult])
    val response = request.send()

    if (response.isSuccess) {
      response.body match {
        case Left(error) =>
          println(s"-- searchBatchUser body ERROR - ${error.getMessage}")
          users
        case Right(searchResult) if searchResult.items.nonEmpty =>
          println(s"-- searchBatchUser searchResult total items - ${searchResult.items.size}")
          val usersList = users ++ searchResult.items
          if (maxRequests == 0 || ((maxRequests - 1) <= usersList.size)) users
          else searchBatchUser(initPage + 1, usersList, maxRequests - 1)
        case Right(searchResult) if searchResult.items.isEmpty => users
      }
    } else {
      println(s"-- searchBatchUser - response ERROR . response: $response")
      users
    }
  }

  @scala.annotation.tailrec
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
  private def nextPageParam(linkHeader: String, paramName: String): Long = {
    println(s"--- linkHeader: $linkHeader")
    val pattern: Regex = raw"""^<https://.+$paramName=(\d+)>$$""".r
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

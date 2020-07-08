package com.app.sourcing.client

import java.util.regex.{Matcher, Pattern}

import com.app.sourcing.ConfigVars
import com.app.sourcing.ConfigVars._
import io.circe.generic.auto._
import sttp.client._
import sttp.client.circe._

import scala.util.matching.Regex

final case class GitHubUser(login: String)

final case class GitHubFullUser(login: String, id: Long, name: String, location: String) {
  override def toString: String = {
    login + "," + id + "," + name + "," + location
  }
}

final case class GitHubRepo(id: Long, languages_url: String)

final case class GitHubRepoOwner(login: String, id: Long)

final case class GitHubFullRepo(
    id: Long,
    name: String,
    owner: GitHubRepoOwner,
    languages_url: String,
    languages: Option[List[String]]) {
  override def toString: String = {
    id + "," + name + "," + owner.login + "," + owner.id + "," + getLanguages(languages)
  }

  private def getLanguages(list: Option[List[String]]): String = {
    val li: List[String] = list.getOrElse(List.empty).filter(_.nonEmpty)
    li.drop(1).foldLeft(li.headOption.getOrElse(""))((a, b) => s"$a-$b")
  }
}

object GitHubClient {

  def apply(): GitHubClient = {
    new GitHubClient()
  }
}

class GitHubClient() {

  implicit val backend: SttpBackend[Identity, Nothing, NothingT] = HttpURLConnectionBackend()

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

  def getUsers(initVal: Long, maxVal: Long): List[GitHubUser] = {
    getBatchUser(initVal, maxVal, List.empty[GitHubUser])
  }

  @scala.annotation.tailrec
  private def getBatchUser(init: Long, max: Long, users: List[GitHubUser]): List[GitHubUser] = {
    println(s"------ getBachUser init: $init, max: $max")

    implicit val backend = HttpURLConnectionBackend()
    val response = basicRequest
      .header(authHeader, s"$tokenType $token")
      .get(uri"$usersUri?$pageUriParam=$init")
      .response(asJson[Seq[GitHubUser]])
    val result = response.send()

    if (result.isSuccess) {
      result.body match {
        case Left(error) =>
          println(s"getBatchUser Error - ${error.getMessage}")
          users
        case Right(value) =>
          val nextBatch = nextSinceParam(
            result.headers.filter(_.name.equals(linkHeader)).head.value.split(";")(0))
          println(s"--- getBachUser next since: $nextBatch")
          if (nextBatch > 0 && nextBatch <= max) {
            getBatchUser(nextBatch, max, users ++ value)
          } else {
            users ++ value
          }
      }
    } else {
      users
    }
  }

  def getRepositories: List[GitHubFullRepo] = {
    getBatchRepository(ConfigVars.reposInitVal, ConfigVars.reposMaxVal, List.empty[GitHubFullRepo])
  }

  @scala.annotation.tailrec
  private def getBatchRepository(
      init: Long,
      max: Long,
      repos: List[GitHubFullRepo]): List[GitHubFullRepo] = {

    implicit val backend = HttpURLConnectionBackend()
    val response = basicRequest
      .header(authHeader, s"$tokenType $token")
      .get(uri"$reposUri?$pageUriParam=$init")
      .response(asJson[Seq[GitHubFullRepo]])
    val result = response.send()

    if (result.isSuccess) {
      result.body match {
        case Left(_) =>
          repos
        case Right(value) =>
          val nextBatch = nextSinceParam(
            result.headers.filter(_.name.equals(linkHeader)).head.value.split(";")(0))
          if (nextBatch > 0 && nextBatch <= max) {
            getBatchRepository(nextBatch, max, repos ++ value)
          } else {
            repos ++ value
          }
      }
    } else {
      repos
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

  def searchLanguage(matcher: Matcher, languages: List[String]): List[String] = {
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

  /** nextSinceParam Rel version. linkHeader differs from verion 1
    * Get uri for next default page.
    * Link header should be like:
    * <https://api.github.com/repositories?since=876>; rel="next", <https://api.github.com/repositories{?since}>; rel="first" */
  private def nextSinceParam2(linkHeader: String) = {
    println(s"--- linkHeader: $linkHeader")
    if (linkHeader.contains(relNextString)) {
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
    } else 0L
  }
}

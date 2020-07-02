package com.app.sourcing.client

import java.util.regex.{Matcher, Pattern}

import io.circe.generic.auto._
import monix.eval.Task
import sttp.client._
import sttp.client.asynchttpclient.monix.AsyncHttpClientMonixBackend
import sttp.client.circe._
import com.app.sourcing.ConfigVars._

case class GitHubUser(login: String)

case class GitHubFullUser(login: String, id: Long, name: String, location: String) {
  override def toString: String = {
    login + "," + id + "," + name + "," + location
  }
}

case class GitHubRepo(id: Long, languages_url: String)

case class GitHubRepoOwner(login: String, id: Long)

case class GitHubFullRepo(
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
    li.tail.foldLeft(li.head)((a, b) => s"$a-$b")
  }
}

object GitHubClient {

  def apply(): GitHubClient = {
    new GitHubClient()
  }

}

class GitHubClient() {

  implicit val asyncBackend = AsyncHttpClientMonixBackend()

  def getUser(users: List[String]): Task[List[Option[GitHubFullUser]]] = {

    asyncBackend.flatMap { implicit backend =>
      val result = users.map { user =>
        val response = basicRequest
          .header(authHeader, s"$tokenType $token")
          .get(uri"$usersUri/$user")
          .response(asJson[GitHubFullUser])
        response.send().flatMap { r =>
          r.body match {
            case Left(error) =>
              Task(None)
            case Right(value) =>
              Task(Some(value))
          }
        }
      }
      Task.parSequence(result)
    }
  }

  def getUsers(): List[GitHubUser] = {
    getBatchUser(0, 100, List.empty[GitHubUser])
  }

  private def getBatchUser(init: Long, max: Long, users: List[GitHubUser]): List[GitHubUser] = {

    val response = basicRequest
      .header(authHeader, s"$tokenType $token")
      .get(uri"$usersUri?$pageUriParam=$init")
      .response(asJson[Seq[GitHubUser]])

    implicit val backend = HttpURLConnectionBackend()
    val result = response.send()

    if (result.isSuccess) {

      result.body match {
        case Left(error) =>
          users
        case Right(value) =>
          val nextBatch = nextSinceParam(
            result.headers.filter(_.name.equals(linkHeader)).head.value.split(";")(0))
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

  def getRepositories(): List[GitHubFullRepo] = {
    getBatchRepository(0, 100, List.empty[GitHubFullRepo])
  }

  private def getBatchRepository(
      init: Long,
      max: Long,
      repos: List[GitHubFullRepo]): List[GitHubFullRepo] = {

    val response = basicRequest
      .header(authHeader, s"$tokenType $token")
      .get(uri"$reposUri?$pageUriParam=$init")
      .response(asJson[Seq[GitHubFullRepo]])

    implicit val backend = HttpURLConnectionBackend()
    val result = response.send()

    if (result.isSuccess) {

      result.body match {
        case Left(error) =>
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

  def getFullRepositories(
      repositories: List[GitHubFullRepo]): Task[List[Option[GitHubFullRepo]]] = {

    asyncBackend.flatMap { implicit backend =>
      val result = repositories.map { repo =>
        val response = basicRequest
          .header(authHeader, s"$tokenType $token")
          .get(uri"${repo.languages_url}")

        response.send().flatMap { r =>
          r.body match {
            case Left(error) =>
              Task(None)
            case Right(value) =>
              if (!value.isEmpty) {
                val languages = extractLanguages(value)
                Task(Some(repo.copy(languages = Some(languages))))
              } else {
                Task(Some(repo.copy(languages = None)))
              }
          }

        }
      }
      Task.parSequence(result)
    }

  }

  private def searchLanguage(matcher: Matcher, languages: List[String]): List[String] = {
    if (matcher.find()) {
      searchLanguage(matcher, matcher.group(1) :: languages)
    } else {
      languages
    }
  }

  private def extractLanguages(languages: String): List[String] = {
    val p = Pattern.compile("\"([^\"]*)\"");
    val m = p.matcher(languages);
    searchLanguage(m, List.empty[String])

  }

  /** Get uri for next default page.
    * Link header should be like:
    * <https://api.github.com/repositories?since=876>; rel="next", <https://api.github.com/repositories{?since}>; rel="first" */
  private def nextSinceParam(linkHeader: String) = {
    if (!linkHeader.contains(relNextString)) None
    val pattern = """^<https://.+since=(\d+)>$""".r
    linkHeader.split(",").toList.head.split(";").toList.head match {
      case pattern(x) => x.toLong
      case _          => 0L
    }
  }

}

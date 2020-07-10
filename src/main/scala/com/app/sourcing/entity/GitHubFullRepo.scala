package com.app.sourcing.entity

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

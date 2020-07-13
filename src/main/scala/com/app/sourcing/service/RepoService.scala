package com.app.sourcing.service

import com.app.sourcing.entity.GitHubFullRepo

trait RepoService {
  def getRepositories(maxRequests: Int, initVal: Long): List[GitHubFullRepo]
  def getFullRepositories(repositories: List[GitHubFullRepo]): List[Option[GitHubFullRepo]]
}

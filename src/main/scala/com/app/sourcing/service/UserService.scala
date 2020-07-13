package com.app.sourcing.service

import com.app.sourcing.entity.{GitHubFullRepo, GitHubFullUser, GitHubUser}

trait UserService {
  def getUsers(maxRequests: Int, initVal: Long): List[GitHubUser]
  def getUser(users: List[String]): List[Option[GitHubFullUser]]
  def searchUsers(maxRequests: Int, initPage: Long): List[GitHubUser]
}

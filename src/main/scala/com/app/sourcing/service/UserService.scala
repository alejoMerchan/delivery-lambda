package com.app.sourcing.service

import com.app.sourcing.entity.{GitHubFullUser, GitHubUser}

trait UserService {
  def getUsers(maxRequests: Int, initVal: Long): List[GitHubUser]
  def getUser(users: List[String]): List[Option[GitHubFullUser]]
  def getBatchUser(init: Long, users: List[GitHubUser], maxRequests: Int): List[GitHubUser]
}

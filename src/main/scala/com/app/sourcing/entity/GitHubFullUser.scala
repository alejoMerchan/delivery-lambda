package com.app.sourcing.entity

final case class GitHubFullUser(login: String, id: Long, name: String, location: String) {
  override def toString: String = {
    login + "," + id + "," + name + "," + location
  }
}

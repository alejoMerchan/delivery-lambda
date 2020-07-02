package com.app.sourcing

import com.typesafe.config.{Config, ConfigFactory}

sealed trait ConfigVars {
  val config: Config = ConfigFactory.load()
  def clientException: String
  def authHeader: String
  def usersUri: String
  def reposUri: String
  def linkHeader: String
  def pageUriParam: String
  def relNextString: String
  def tokenType: String
  def token: String
  def bucketName: String
  def reposFilename: String
  def usersFilename: String
}

object ConfigVars extends ConfigVars {
  def clientException: String = config.getString("client-exception")
  def authHeader: String = config.getString("auth-header")
  def usersUri: String = config.getString("users-uri")
  def reposUri: String = config.getString("repos-uri")
  def linkHeader: String = config.getString("link-header")
  def pageUriParam: String = config.getString("page-uri-param")
  def relNextString: String = config.getString("rel-next-string")
  def tokenType: String = config.getString("token-type")
  def token: String = config.getString("token")
  def bucketName: String = config.getString("bucket-name")
  def reposFilename: String = config.getString("repos-filename")
  def usersFilename: String = config.getString("users-filename")
}

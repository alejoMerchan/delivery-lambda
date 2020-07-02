package com.app.sourcing

import com.typesafe.config.{Config, ConfigFactory}

trait ConfigVars {
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
  override def clientException: String = config.getString("client-exception")
  override def authHeader: String = config.getString("auth-header")
  override def usersUri: String = config.getString("users-uri")
  override def reposUri: String = config.getString("repos-uri")
  override def linkHeader: String = config.getString("link-header")
  override def pageUriParam: String = config.getString("page-uri-param")
  override def relNextString: String = config.getString("rel-next-string")
  override def tokenType: String = config.getString("token-type")
  override def token: String = config.getString("token")
  override def bucketName: String = config.getString("bucket-name")
  override def reposFilename: String = config.getString("repos-filename")
  override def usersFilename: String = config.getString("users-filename")
}

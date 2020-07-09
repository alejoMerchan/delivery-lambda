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
  def usersHeaderLine: String
  def reposHeaderLine: String
  def regionName: String
  def usersMaxRequests: Int
  def reposMaxRequests: Int
}

object ConfigVars extends ConfigVars {
  def clientException: String = config.getString("client.client-exception")
  def authHeader:      String = config.getString("auth.auth-header")
  def usersUri:        String = config.getString("users.users-uri")
  def reposUri:        String = config.getString("repos.repos-uri")
  def linkHeader:      String = config.getString("api-params.link-header")
  def pageUriParam:    String = config.getString("api-params.page-uri-param")
  def relNextString:   String = config.getString("api-params.rel-next-string")
  def tokenType:       String = config.getString("auth.token-type")
  def token:           String = config.getString("auth.token")
  def bucketName:      String = config.getString("bucket.bucket-name")
  def reposFilename:   String = config.getString("repos.repos-filename")
  def usersFilename:   String = config.getString("users.users-filename")
  def usersHeaderLine: String = config.getString("users.users-header-line")
  def reposHeaderLine: String = config.getString("repos.repos-header-line")
  def regionName: String = config.getString("bucket.region-name")
  def usersMaxRequests: Int = config.getInt("users.max-requests")
  def reposMaxRequests: Int = config.getInt("repos.max-requests")
}

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
  def usersInitVal: Int
  def usersMaxVal: Int
  def reposInitVal: Int
  def reposMaxVal: Int
  def usersHeaderLine: String
  def reposHeaderLine: String
  def regionName: String
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
  def usersInitVal:    Int    = config.getInt("users.users-init-val")
  def usersMaxVal:     Int    = config.getInt("users.users-max-val")
  def reposInitVal:    Int    = config.getInt("repos.repos-init-val")
  def reposMaxVal:     Int    = config.getInt("repos.repos-max-val")
  def usersHeaderLine: String = config.getString("users.users-header-line")
  def reposHeaderLine: String = config.getString("repos.repos-header-line")
  def regionName: String = config.getString("bucket.region-name")
}

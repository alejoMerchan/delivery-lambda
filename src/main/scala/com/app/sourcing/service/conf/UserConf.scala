package com.app.sourcing.service.conf

trait UserConf extends ConfigLoad {
  def usersUri: String
  def usersFilename: String
  def usersHeaderLine: String
  def usersMaxRequests: Int
  def initVal: Long
  def maxSearchRequests: Int
  def searchUri: String
  def searchInitPage: Int
  def usersSearchFilename: String
}

object UserConf extends UserConf {
  def usersUri:        String = config.getString("users.users-uri")
  def usersFilename:   String = config.getString("users.users-filename")
  def usersHeaderLine: String = config.getString("users.users-header-line")
  def usersMaxRequests: Int = config.getInt("users.max-requests")
  def initVal: Long = config.getLong("users.init-val")
  def maxSearchRequests: Int = config.getInt("users.max-search-requests")
  def searchUri: String = config.getString("users.search-uri")
  def searchInitPage: Int = config.getInt("users.search-init-page")
  def usersSearchFilename: String = config.getString("users.users-search-filename")
}
package com.app.sourcing.service.conf

trait UserConf extends ConfigLoad {
  def usersUri: String
  def usersFilename: String
  def usersHeaderLine: String
  def usersMaxRequests: Int
  def initVal: Long
}

object UserConf extends UserConf {
  def usersUri:        String = config.getString("users.users-uri")
  def usersFilename:   String = config.getString("users.users-filename")
  def usersHeaderLine: String = config.getString("users.users-header-line")
  def usersMaxRequests: Int = config.getInt("users.max-requests")
  def initVal: Long = config.getLong("users.init-val")
}
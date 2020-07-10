package com.app.sourcing.service.conf

trait AuthConf extends ConfigLoad {
  def authHeader: String
  def tokenType: String
  def token: String
}

object AuthConf extends AuthConf {
  def authHeader:      String = config.getString("auth.auth-header")
  def tokenType:       String = config.getString("auth.token-type")
  def token:           String = config.getString("auth.token")
}

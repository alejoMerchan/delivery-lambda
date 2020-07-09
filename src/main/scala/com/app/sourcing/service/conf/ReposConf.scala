package com.app.sourcing.service.conf

trait ReposConf extends ConfigLoad {
  def reposUri: String
  def reposFilename: String
  def reposHeaderLine: String
  def reposMaxRequests: Int
}

object ReposConf extends ReposConf {
  def reposUri: String = config.getString("repos.repos-uri")
  def reposFilename: String = config.getString("repos.repos-filename")
  def reposHeaderLine: String = config.getString("repos.repos-header-line")
  def reposMaxRequests: Int = config.getInt("repos.max-requests")
}

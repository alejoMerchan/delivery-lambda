package com.app.sourcing.service.conf

trait ApiParamsConf extends ConfigLoad {
  def linkHeader: String
  def pageUriParam: String
  def relNextString: String
}

object ApiParamsConf extends ApiParamsConf {
  def linkHeader: String = config.getString("api-params.link-header")
  def pageUriParam: String = config.getString("api-params.page-uri-param")
  def relNextString: String = config.getString("api-params.rel-next-string")
}

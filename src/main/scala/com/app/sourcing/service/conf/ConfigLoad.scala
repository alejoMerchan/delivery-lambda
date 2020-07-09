package com.app.sourcing.service.conf

import com.typesafe.config.{Config, ConfigFactory}

trait ConfigLoad {
  def config: Config = ConfigFactory.load()
}

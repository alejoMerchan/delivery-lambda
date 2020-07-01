package com.app.sourcing.client

import cats.effect.IO
import com.typesafe.config.{Config, ConfigFactory}

trait Client {

  val config: Config = ConfigFactory.load()
  val clientException: String = config.getString("client-exception")

  case class ClientException(msg:String,exception: Throwable) extends Exception

  def safeCall[A](f:() => A,exception:String = clientException):IO[A] = {
    IO{
      f.apply()
    }.handleErrorWith{
      error =>
        IO.raiseError(ClientException(exception,error))
    }
  }
}

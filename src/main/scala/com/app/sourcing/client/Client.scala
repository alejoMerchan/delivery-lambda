package com.app.sourcing.client

import cats.effect.IO

trait Client {

  case class ClientException(msg:String,exception: Throwable) extends Exception

  def safeCall[A](f:() => A,exception:String = "Client Exception"):IO[A] = {
    IO{
      f.apply()
    }.handleErrorWith{
      error =>
        IO.raiseError(ClientException(exception,error))
    }
  }

}

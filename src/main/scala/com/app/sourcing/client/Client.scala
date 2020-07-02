package com.app.sourcing.client

import cats.effect.IO
import com.app.sourcing.ConfigVars.clientException

trait Client {

  case class ClientException(msg:String,exception: Throwable) extends Exception

  def safeCall[A](f:() => A,exception:String = clientException): IO[A] = {
    IO{
      f.apply()
    }.handleErrorWith{
      error =>
        IO.raiseError(ClientException(exception,error))
    }
  }
}

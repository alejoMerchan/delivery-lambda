package com.app.sourcing

trait Failure {
  def exception: Throwable
}

object Failure {
  final case class ClientException(msg: String, exception: Throwable) extends Throwable with Failure
}

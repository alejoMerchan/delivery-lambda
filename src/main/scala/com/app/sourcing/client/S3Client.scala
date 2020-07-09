package com.app.sourcing.client

import cats.effect.IO
import com.app.sourcing.Failure.ClientException

trait S3Client {

  def safeCall[A](f: () => A, exception: String = "clientException"): IO[A] = {
    IO {
      f.apply()
    }.handleErrorWith { error =>
      IO.raiseError(ClientException(exception, error))
    }
  }
}

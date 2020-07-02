package com.app

import cats.effect.IO
import monix.eval.Task

package object sourcing {

  type TaskIOList[A] = Task[IO[List[A]]]
  type TaskListOption[A] = Task[List[Option[A]]]

}

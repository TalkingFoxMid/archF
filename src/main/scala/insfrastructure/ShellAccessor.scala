package insfrastructure

import cats.effect.IO
import cats.syntax.flatMap._
import cats.syntax.functor._

import scala.sys.process._

import cats.effect.Sync

class ShellAccessor[F[_]: Sync] {
  private val yesProcessF: F[ProcessBuilder] = Sync[F].delay(Process("echo yes"))

  def execCommand(command: String): F[String] =
    Sync[F].delay(command.!!)

  def execCommandYes(command:String): F[String] =
    for {
      yesProcess <- yesProcessF
      command <- Sync[F].delay(Process(command))
    } yield (yesProcess #| command).!!
}
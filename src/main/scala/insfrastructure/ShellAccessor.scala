package insfrastructure
import cats.effect.IO
import cats.syntax.flatMap._
import cats.syntax.functor._

import scala.sys.process._
import cats.effect.Sync

import java.io.File


class ShellAccessor[F[_]: Sync] {
  private val yesProcessF: F[ProcessBuilder] = Sync[F].delay(Process("echo yes"))

  def execCommandWithEcho(command: String, echoString: String): F[String] =
    for {
      yesProcess <- Sync[F].delay(Process(s"echo $echoString"))
      command <- Sync[F].delay(Process(command))
    } yield (yesProcess #| command).!!

  def execCommand(command: String): F[String] =
    Sync[F].delay(command.!!)

  def execCommandYes(command:String): F[String] =
    for {
      yesProcess <- yesProcessF
      command <- Sync[F].delay(Process(command))
    } yield (yesProcess #| command).!!

  def execShellCommand(shellCommand: ShellCommand): F[String] =
    Sync[F].delay(buildProcess(shellCommand).!!(new ProcessLogger {
      override def out(s: => String): Unit = println(s)

      override def err(s: => String): Unit = println(s)

      override def buffer[T](f: => T): T = f
    }))

  private def buildProcess(shellCommand: ShellCommand): ProcessBuilder = shellCommand match {
    case BasicShellCommand(command, directory) => directory match {
      case Some(value) => Process(command, new File(value))
      case None => Process(command)
    }
    case PipeShellCommand(from, to) => buildProcess(from) #| buildProcess(to)
  }
}
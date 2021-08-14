package Funcman

import cats.syntax.traverse._
import cats.syntax.flatMap._
import cats.syntax.applicative._
import cats.{Applicative, Functor}
import cats.effect.{IO, Sync}
import insfrastructure.ShellAccessor
import cats.syntax.functor._

import scala.sys.process._

trait PacmanApi[F[_]] {
  def packageList: F[Set[String]]
}

class PacmanApiImpl[F[_]: Sync](implicit shellAccessor: ShellAccessor[F]) extends PacmanApi[F] {
  override def packageList: F[Set[String]] =
    for {
      string <- shellAccessor.execCommand("pacman -Q")
    } yield string.split("\n")
      .map(_.split(" "))
      .collect {
        case Array(a, _) => a
      }.toSet

  def updateAll: F[String] =
    shellAccessor.execCommandYes("pacman -Syu")

  def installPackage(packages: Set[String]): F[Unit] =
    shellAccessor.execCommandYes(s"pacman -S ${packages.mkString(" ")}").void
      .whenA(packages.nonEmpty)

  def removePackage(packages: Set[String]): F[Unit] = {
    shellAccessor.execCommandYes(s"pacman -R ${packages.mkString(" ")}").void
      .whenA(packages.nonEmpty)
  }
}

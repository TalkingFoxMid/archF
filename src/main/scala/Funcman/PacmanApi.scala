package Funcman
import PackagesOps._
import cats.syntax.traverse._
import cats.syntax.flatMap._
import cats.syntax.applicative._

import cats.{Applicative, Functor}
import cats.effect.{IO, Sync}
import insfrastructure.ShellAccessor
import cats.syntax.functor._

import scala.sys.process._

trait PacmanApi[F[_]] {
  def packageList: F[Set[Package]]

  def getDependencies(packageF: Package): F[Set[Package]]

  def getDependenciesSync(packageF: Package): F[Set[Package]]

  def getCodependencies(packageF: String): F[Set[String]]

  def existsSync(packageF: String): F[Boolean]

  def installPackage(packages: Set[Package]): F[Unit]

  def removePackage(packages: Set[Package]): F[Unit]

  def updateAll: F[String]

  def getPackagesInGroup(group: String): F[Set[String]]
}

class PacmanApiImpl[F[_]: Sync](implicit shellAccessor: ShellAccessor[F]) extends PacmanApi[F] {
  override def packageList: F[Set[Package]] =
    for {
      string <- shellAccessor.execCommand("pacman -Q")
    } yield string.split("\n")
      .map(_.split(" "))
      .collect {
        case Array(a, _) => Package(a)
      }.toSet

  def updateAll: F[String] =
    shellAccessor.execCommandYes("pacman -Syu")

  def installPackage(packages: Set[Package]): F[Unit] =
    shellAccessor.execCommandYes(s"pacman -S ${packages.map(_.name).mkString(" ")}").void
      .whenA(packages.nonEmpty)

  def removePackage(packages: Set[Package]): F[Unit] = {
    shellAccessor.execCommandYes(s"pacman -R ${packages.map(_.name).mkString(" ")}").void
      .whenA(packages.nonEmpty)
  }

  def getDependencies(packageF: Package): F[Set[Package]] =
    shellAccessor.execCommand(s"pactree -u ${packageF.name}")
      .map(_.asPackages.map(Package))

  def getDependenciesSync(packageF: Package): F[Set[Package]]=
    shellAccessor.execCommand(s"pactree -u -s ${packageF.name}")
      .map(_.asPackages.map(Package))

  def getCodependencies(packageF: String): F[Set[String]] =
    shellAccessor.execCommand(s"pactree -u -r $packageF")
      .map(_.asPackages)

  def existsSync(packageF: String): F[Boolean] = {
    shellAccessor.execCommand(s"pactree -u -s -d 0 $packageF")
      .map(_ equals packageF)
  }

  def getPackagesInGroup(group: String): F[Set[String]] =
    shellAccessor.execCommand(s"pacman -Sg $group")
      .map(_.asGroup)
}

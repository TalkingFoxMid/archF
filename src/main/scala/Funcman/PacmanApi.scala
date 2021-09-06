package Funcman
import PackagesOps._
import cats.syntax.traverse._
import cats.syntax.flatMap._
import cats.syntax.applicative._
import cats.{Applicative, Functor, MonadError}
import cats.effect.{ExitCode, IO, IOApp, Sync}
import insfrastructure.ShellAccessor
import cats.syntax.functor._

import tfox.immersivecollections.instances.set._

import scala.sys.process._

case class PackageVerifyException(name: String) extends Exception(name)

trait PacmanApi[F[_]] {
  def verifyPackage(name: String): F[PacmanPackage]

  def packageList: F[Set[PacmanPackage]]

  def getDependencies(packageF: PacmanPackage): F[Set[PacmanPackage]]

  def getDependenciesSync(packageF: PacmanPackage): F[Set[PacmanPackage]]

  def getCodependencies(packageF: String): F[Set[String]]

  def existsSync(packageF: String): F[Boolean]

  def installPackage(packages: Set[PacmanPackage]): F[Unit]

  def removePackage(packages: Set[PacmanPackage]): F[Unit]

  def updateAll: F[String]

  def getPackagesInGroup(group: String): F[Set[PacmanPackage]]
}

class PacmanApiImpl[F[_]: Sync](implicit shellAccessor: ShellAccessor[F]) extends PacmanApi[F] {
  private case class VerifiedPackageImpl(name: String) extends PacmanPackage

  def verifyPackage(name: String): F[PacmanPackage] =
    for {
      exists <- existsSync(name)
      _ <- MonadError[F, Throwable].raiseError(PackageVerifyException(name))
        .whenA(!exists)
    } yield VerifiedPackageImpl(name)

  override def packageList: F[Set[PacmanPackage]] =
    for {
      string <- shellAccessor.execCommand("pacman -Q")
    } yield string.split("\n")
      .map(_.split(" "))
      .collect {
        case Array(a, _) => VerifiedPackageImpl(a)
      }.toSet

  def updateAll: F[String] =
    shellAccessor.execCommandYes("pacman -Syu")

  def installPackage(packages: Set[PacmanPackage]): F[Unit] =
    shellAccessor.execCommandYes(s"pacman -S ${packages.map(_.name).mkString(" ")}").void
      .whenA(packages.nonEmpty)

  def removePackage(packages: Set[PacmanPackage]): F[Unit] = {
    shellAccessor.execCommandYes(s"pacman -R ${packages.map(_.name).mkString(" ")}").void
      .whenA(packages.nonEmpty)
  }

  def getDependencies(packageF: PacmanPackage): F[Set[PacmanPackage]] =
    shellAccessor.execCommand(s"pactree -u ${packageF.name}")
      .map(_.asPackages.map(VerifiedPackageImpl))

  def getDependenciesSync(packageF: PacmanPackage): F[Set[PacmanPackage]]=
    shellAccessor.execCommand(s"pactree -u -s ${packageF.name}")
      .map(_.asPackages.map(VerifiedPackageImpl))

  def getCodependencies(packageF: String): F[Set[String]] =
    shellAccessor.execCommand(s"pactree -u -r $packageF")
      .map(_.asPackages)

  def existsSync(packageName: String): F[Boolean] =
    shellAccessor.execCommand(s"pactree -u -s -d 0 $packageName")
      .map(_.trim equals packageName)

  def getPackagesInGroup(group: String): F[Set[PacmanPackage]] =
    for {
      packageNames <- shellAccessor.execCommand(s"pacman -Sg $group")
        .map(_.asGroup)
      _ <- MonadError[F, Throwable].raiseError(FuncmanException(s"There is no group: $group"))
        .whenA(packageNames.isEmpty)
      packages <- packageNames.traverse(verifyPackage)
    } yield packages
}
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
  def verifyPackage(name: String): F[VerifiedPackage]

  def packageList: F[Set[VerifiedPackage]]

  def getDependencies(packageF: VerifiedPackage): F[Set[VerifiedPackage]]

  def getDependenciesSync(packageF: VerifiedPackage): F[Set[VerifiedPackage]]

  def getCodependencies(packageF: String): F[Set[String]]

  def existsSync(packageF: String): F[Boolean]

  def installPackage(packages: Set[VerifiedPackage]): F[Unit]

  def removePackage(packages: Set[VerifiedPackage]): F[Unit]

  def updateAll: F[String]

  def getPackagesInGroup(group: String): F[Set[VerifiedPackage]]
}

class PacmanApiImpl[F[_]: Sync](implicit shellAccessor: ShellAccessor[F]) extends PacmanApi[F] {
  private case class VerifiedPackageImpl(name: String) extends VerifiedPackage

  def verifyPackage(name: String): F[VerifiedPackage] =
    Sync[F].delay(VerifiedPackageImpl(name))
//    for {
//      exists <- existsSync(name)
//      _ <- MonadError[F, Throwable].raiseError(PackageVerifyException(name))
//        .whenA(!exists)
//    } yield VerifiedPackageImpl(name)

  override def packageList: F[Set[VerifiedPackage]] =
    for {
      string <- shellAccessor.execCommand("pacman -Q")
    } yield string.split("\n")
      .map(_.split(" "))
      .collect {
        case Array(a, _) => VerifiedPackageImpl(a)
      }.toSet

  def updateAll: F[String] =
    shellAccessor.execCommandYes("pacman -Syu")

  def installPackage(packages: Set[VerifiedPackage]): F[Unit] =
    shellAccessor.execCommandYes(s"pacman -S ${packages.map(_.name).mkString(" ")}").void
      .whenA(packages.nonEmpty)

  def removePackage(packages: Set[VerifiedPackage]): F[Unit] = {
    shellAccessor.execCommandYes(s"pacman -R ${packages.map(_.name).mkString(" ")}").void
      .whenA(packages.nonEmpty)
  }

  def getDependencies(packageF: VerifiedPackage): F[Set[VerifiedPackage]] =
    shellAccessor.execCommand(s"pactree -u ${packageF.name}")
      .map(_.asPackages.map(VerifiedPackageImpl))

  def getDependenciesSync(packageF: VerifiedPackage): F[Set[VerifiedPackage]]=
    shellAccessor.execCommand(s"pactree -u -s ${packageF.name}")
      .map(_.asPackages.map(VerifiedPackageImpl))

  def getCodependencies(packageF: String): F[Set[String]] =
    shellAccessor.execCommand(s"pactree -u -r $packageF")
      .map(_.asPackages)

  def existsSync(packageName: String): F[Boolean] =
    shellAccessor.execCommand(s"pactree -u -s -d 0 $packageName")
      .map(_.trim equals packageName)

  def getPackagesInGroup(group: String): F[Set[VerifiedPackage]] =
    for {
      packageNames <- shellAccessor.execCommand(s"pacman -Sg $group")
        .map(_.asGroup)
      _ <- MonadError[F, Throwable].raiseError(FuncmanException(s"There is no group: $group"))
        .whenA(packageNames.isEmpty)
      packages <- packageNames.traverse(verifyPackage)
    } yield packages
}
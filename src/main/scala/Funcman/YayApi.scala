package Funcman

import Funcman.YayApiImpl.aurPrefix
import cats.{Functor, Monad, MonadError, MonadThrow}
import insfrastructure.ShellAccessor
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.syntax.applicativeError._
import insfrastructure._
import cats.syntax.option._

trait YayApi[F[_]] {
  def installWithYay(pkg: AurPackage): F[Unit]
}

class YayApiImpl[F[_]: MonadThrow](implicit shellAccessor: ShellAccessor[F]) extends YayApi[F] {
  override def installWithYay(pkg: AurPackage): F[Unit] =
    for {
      _ <- shellAccessor.execShellCommand(BasicShellCommand(s"git clone ${aurPrefix(pkg.name)} /home/flower/archf_temp/${pkg.name}"))
        .void
        .handleErrorWith(_ => MonadError[F, Throwable].unit)
      _ <- shellAccessor.execShellCommand(BasicShellCommand("echo y")|BasicShellCommand(s"su flower makepkg -sri",
        s"/home/flower/archf_temp/${pkg.name}".some))
    } yield ()
}

object YayApiImpl {
  def aurPrefix(pkgName: String): String = s"https://aur.archlinux.org/$pkgName.git "
}
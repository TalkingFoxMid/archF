package appliers

import Funcman.{DiffPackage, PacmanApi, PacmanService}
import cats.Monad
import cats.effect.ExitCode
import cats.syntax.flatMap._
import cats.syntax.functor._

class PacmanApplier[F[_]: Monad](implicit pacmanService: PacmanService[F],
                                 pacmanApi: PacmanApi[F]) extends Applier[F] {
  override def apply: F[Unit] =
    for {
      _ <- pacmanApi.updateAll
      DiffPackage(toInstall, toDelete) <- pacmanService.getChanges
      _ <- pacmanApi.removePackage(toDelete)
      _ <- pacmanApi.installPackage(toInstall)
    } yield ()
}
object PacmanApplier {
  def apply[F[_] : Monad](implicit pacmanService: PacmanService[F], pacmanApi: PacmanApi[F]): PacmanApplier[F] = new PacmanApplier
}
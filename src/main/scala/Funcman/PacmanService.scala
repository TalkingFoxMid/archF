package Funcman

import Configurators.PacmanConfig
import cats.data.Kleisli
import cats.{Applicative, FlatMap, Monad, MonadError, MonadThrow}
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.applicative._
import cats.syntax.traverse._
import cats.syntax.applicativeError._
import tfox.immersivecollections.instances.set._

case class DiffPackage(toInstall: Set[PacmanPackage], toRemove: Set[PacmanPackage])

class PacmanService[F[_]: MonadThrow](implicit pacmanApi: PacmanApi[F],
                                      pacmanConfig: PacmanConfig[F]) {
  val getDependencies = Kleisli(pacmanApi.getDependencies)
  val getDependenciesSync = Kleisli(pacmanApi.getDependenciesSync)

  def dependify(packageList: Set[PacmanPackage]): F[Set[PacmanPackage]] =
    packageList.flatTraverse(getDependencies.handleErrorWith(_ => getDependenciesSync).run)

  def getChanges: F[DiffPackage] =
    for {
      oldPackages <- pacmanApi.packageList

      newPackages <- pacmanConfig.getPackagesSetup
        .flatMap(_.traverse(pacmanApi.verifyPackage))
        .flatMap(dependify)

      newGroupsPackages <- pacmanConfig.getGroupsSetup
        .flatMap(_.flatTraverse(pacmanApi.getPackagesInGroup))
        .flatMap(dependify)
    } yield getDiffs(oldPackages, newPackages union newGroupsPackages)

  private def getDiffs(oldPack: Set[PacmanPackage], newPack: Set[PacmanPackage]): DiffPackage =
    DiffPackage(newPack -- oldPack, oldPack -- newPack)
}

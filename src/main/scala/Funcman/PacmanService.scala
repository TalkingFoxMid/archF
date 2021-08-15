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

case class DiffPackage(toInstall: Set[Package], toRemove: Set[Package])

class PacmanService[F[_]: MonadThrow](implicit pacmanApi: PacmanApi[F],
                                      pacmanConfig: PacmanConfig[F]) {
  val getDependencies = Kleisli(pacmanApi.getDependencies)
  val getDependenciesSync = Kleisli(pacmanApi.getDependenciesSync)

  def dependify(packageList: Set[Package]): F[Set[Package]] =
    packageList.flatTraverse(getDependencies.handleErrorWith(_ => getDependenciesSync).run)

  def getChanges: F[DiffPackage] =
    for {
      oldPackages <- pacmanApi.packageList

      newPackages <- pacmanConfig.getPackagesSetup.flatMap(_.traverse(packagefy))
      newGroupsPackages <- pacmanConfig.getGroupsSetup.flatMap(_.flatTraverse(groupify))
    } yield getDiffs(oldPackages, newPackages union newGroupsPackages)

  private def getDiffs(oldPack: Set[Package], newPack: Set[Package]): DiffPackage =
    DiffPackage(newPack -- oldPack, oldPack -- newPack)

  private def packagefy(name: String): F[Package] =
    for {
      exists <- pacmanApi.existsSync(name)
      _ <- MonadError[F, Throwable].raiseError(FuncmanException(s"There is no package: $name"))
        .whenA(!exists)
    } yield Package(name)

  private def groupify(name: String): F[Set[Package]] =
    for {
      packageNames <- pacmanApi.getPackagesInGroup(name)
      _ <- MonadError[F, Throwable].raiseError(FuncmanException(s"There is no group: $name"))
        .whenA(packageNames.isEmpty)
      packages <- packageNames.traverse(packagefy)
    } yield packages
}

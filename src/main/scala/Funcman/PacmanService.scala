package Funcman

import Configurators.PacmanConfig
import cats.{Applicative, FlatMap, Monad}
import cats.effect.IO
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.applicative._
import cats.syntax.traverse._
import tfox.immersivecollections.instances.set._

case class DiffPackage(toInstall: Set[String], toRemove: Set[String])

class PacmanService[F[_]: Monad](implicit pacmanApi: PacmanApi[F],
                                 pacmanConfig: PacmanConfig[F]) {
  def dependify(packageList: Set[String]): F[Set[String]] =
    packageList.flatTraverse(pacmanApi.getDependencies)

  def getChanges: F[DiffPackage] =
    for {
      oldPack <- pacmanApi.packageList
      newPack <- pacmanConfig.getCustomPackages >>= dependify
      base <- pacmanConfig.getBasePackages >>= dependify
    } yield getDiffs(oldPack, newPack union base)

  private def getDiffs(oldPack: Set[String], newPack: Set[String]): DiffPackage =
    DiffPackage(newPack -- oldPack, oldPack -- newPack)
}

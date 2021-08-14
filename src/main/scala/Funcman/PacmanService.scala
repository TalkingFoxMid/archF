package Funcman

import cats.{Applicative, FlatMap}
import cats.effect.IO
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.instances.all._
import cats.syntax.traverse._
import tfox.immersivecollections.instances.set._

class PacmanService[F[_]: Applicative](implicit pacmanApi: PacmanApi[F]) {
  def dependify(packageList: Set[String]): F[Set[String]] =
    packageList.flatTraverse(pacmanApi.getDependencies)
}

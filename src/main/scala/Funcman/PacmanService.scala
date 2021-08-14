package Funcman

import cats.{Applicative, FlatMap}
import cats.effect.IO
import cats.instances.all._
import cats.syntax.traverse._
import Implicits.CollectionsOps._

class PacmanService[F[_]: Applicative](implicit pacmanApi: PacmanApi[F]) {
  def dependify(packageList: Set[String]): F[Set[String]] = {
    val g = packageList.flatTraverse(pacmanApi.getDependencies)
  }
}

package appliers

import cats.Applicative
import cats.effect.IO
import cats.syntax.flatMap._
import cats.syntax.functor._

import tfox.immersivecollections.syntax.applicative._

trait Applier[F[_]] {
  def apply: F[Unit]
}

object Applier {
  implicit class ApplierOps[F[_]: Applicative](private val applier: Applier[F]) {
    def ::(other: Applier[F]): Applier[F] =
      new Applier[F] {
        override def apply: F[Unit] = applier.apply.product(other.apply).map(_ => ())
      }
  }
}
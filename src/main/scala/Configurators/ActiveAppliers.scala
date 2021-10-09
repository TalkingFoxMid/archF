package Configurators

import Funcman.PacmanApplier
import appliers.{Applier, Services}
import cats.Monad
import cats.effect.Sync

trait Appliers[F[_]] {
  def getAppliers: Applier[F]
}
class ActiveAppliers[F[_]: Sync] extends Appliers[F] {
  val services = new Services[F]
  import services._

  override def getAppliers: Applier[F] =
    PacmanApplier[F]
}

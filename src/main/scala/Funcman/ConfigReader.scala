package Funcman

import cats.effect.kernel.Sync
import PackagesOps._
import scala.io.Source

class ConfigReader[F[_]: Sync] {
  val getPackagesToBe: F[Set[String]] =
    Sync[F].delay {
      Source.fromResource("configs/package.list")
        .mkString.asPackages
    }
}

package Funcman

import cats.effect.IO
import cats.syntax.applicative._

case class DiffPackage(toInstall: Set[String], toRemove: Set[String])

class DiffManager {
  def getDiffs(oldPack: Set[String], newPack: Set[String]): IO[DiffPackage] = {
    DiffPackage(newPack -- oldPack, oldPack -- newPack).pure[IO]
  }
}
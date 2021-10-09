import Configurators.{ActiveAppliers, PacmanConfig, PacmanConfigImpl}
import Funcman.{DiffPackage, PacmanApi, PacmanApiImpl, PacmanApplier, PacmanService}
import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.traverse._
import insfrastructure.ShellAccessor
import cats.syntax.applicative._
import cats.syntax.traverse._
import tfox.immersivecollections.instances.set._

import scala.sys.process._
import appliers.Applier._

object Apply extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val appliers = new ActiveAppliers[IO]

    for {
      _ <- appliers.getAppliers.apply
    } yield ExitCode.Success
  }
}

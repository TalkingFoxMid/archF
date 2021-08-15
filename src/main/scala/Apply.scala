import Configurators.{PacmanConfig, PacmanConfigImpl}
import Funcman.{DiffPackage, PacmanApi, PacmanApiImpl, PacmanService}
import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.traverse._
import insfrastructure.ShellAccessor
import cats.syntax.applicative._
import cats.syntax.traverse._
import tfox.immersivecollections.instances.set._

import scala.sys.process._
import appliers.Applier._
import appliers.PacmanApplier

object Apply extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    implicit val shellAccessor = new ShellAccessor[IO]
    implicit val pacmanApi: PacmanApiImpl[IO] = new PacmanApiImpl[IO]()
    implicit val pacmanConfig: PacmanConfig[IO] = new PacmanConfigImpl[IO]
    implicit val pacmanService = new PacmanService[IO]
    val appliers = PacmanApplier[IO]

    for {
      _ <- appliers.apply
    } yield ExitCode.Success
  }
}

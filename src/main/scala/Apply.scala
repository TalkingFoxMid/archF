import Configurators.{PacmanConfig, PacmanConfigImpl}
import Funcman.{DiffPackage, PacmanApi, PacmanApiImpl, PacmanService}
import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.traverse._
import insfrastructure.ShellAccessor
import cats.syntax.applicative._
import cats.syntax.traverse._
import tfox.immersivecollections.instances.set._
import scala.sys.process._

object Apply extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    implicit val shellAccessor = new ShellAccessor[IO]
    implicit val pacmanApi = new PacmanApiImpl[IO]()
    implicit val pacmanConfig: PacmanConfig[IO] = new PacmanConfigImpl
    val pacmanService = new PacmanService[IO]
    for {
      DiffPackage(toInstall, toDelete) <- pacmanService.getChanges
      _ <- pacmanApi.removePackage(toDelete)
      _ <- pacmanApi.installPackage(toInstall)
    } yield ExitCode.Success
  }
}

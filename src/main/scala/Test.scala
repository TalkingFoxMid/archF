import Configurators.{PacmanConfig, PacmanConfigImpl}
import Funcman.{AurPackage, PacmanApiImpl, PacmanApplier, PacmanService, YayApi, YayApiImpl}
import cats.effect.{ExitCode, IO, IOApp}
import insfrastructure.ShellAccessor

object Test extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    implicit val shellAccessor = new ShellAccessor[IO]
    implicit val pacmanApi: PacmanApiImpl[IO] = new PacmanApiImpl[IO]()
    implicit val pacmanConfig: PacmanConfig[IO] = new PacmanConfigImpl[IO]
    implicit val pacmanService = new PacmanService[IO]
    implicit val yayApi = new YayApiImpl[IO]()
    for {
      _ <- yayApi.installWithYay(new AurPackage{
        override def name: String = "gitkraken"
      })
    } yield ExitCode.Success
  }
}

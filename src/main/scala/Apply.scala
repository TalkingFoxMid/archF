import Funcman.{ConfigReader, DiffManager, DiffPackage, PacmanApi, PacmanApiImpl}
import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.traverse._
import insfrastructure.ShellAccessor

import scala.sys.process._

object Apply extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    for {
      implicit0(sa: ShellAccessor[IO]) <- IO(new ShellAccessor[IO])
      pacmanApi = new PacmanApiImpl[IO]
      cr = new ConfigReader[IO]
      old <- pacmanApi.packageList
      newPackages <- cr.getPackagesToBe
      diffManager = new DiffManager
      diffs <- diffManager.getDiffs(old, newPackages)
      _ <- diffs match {
        case DiffPackage(toInstall, toRemove) => pacmanApi.installPackage(toInstall) >>
          pacmanApi.removePackage(toRemove)
      }
    } yield ExitCode.Success
}

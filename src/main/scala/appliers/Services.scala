package appliers

import Configurators.{PacmanConfig, PacmanConfigImpl}
import Funcman.{PacmanApiImpl, PacmanService}
import cats.effect.kernel.Sync
import insfrastructure.ShellAccessor

class Services[F[_]: Sync] {
  implicit val shellAccessor = new ShellAccessor[F]
  implicit val pacmanApi: PacmanApiImpl[F] = new PacmanApiImpl[F]()
  implicit val pacmanConfig: PacmanConfig[F] = new PacmanConfigImpl[F]
  implicit val pacmanService = new PacmanService[F]
}

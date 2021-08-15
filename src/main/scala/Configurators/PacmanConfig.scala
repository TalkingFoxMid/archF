package Configurators

import Funcman.PacmanApi
import cats.Applicative
import cats.syntax.applicative._

trait PacmanConfig[F[_]] {
  def getPackagesSetup: F[Set[String]]

  def getGroupsSetup: F[Set[String]]
}

class PacmanConfigImpl[F[_]: Applicative](pacmanApi: PacmanApi[F]) extends PacmanConfig[F] {
  private val LinuxBase = Set("base", "linux", "linux-firmware")
  private val ArchFBase = Set("git", "scala", "sbt", "jdk8-openjdk", "pacman-contrib")
  private val Ethernet = Set("dhcpcd")
  private val MyPackages = Set("vim", "chromium")

  private val packages = LinuxBase union ArchFBase union Ethernet union MyPackages

  private val Groups: Set[String] = Set()

  def getPackagesSetup: F[Set[String]] = packages.pure

  def getGroupsSetup: F[Set[String]] = Groups.pure
}

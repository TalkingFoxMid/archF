package Configurators

import cats.Applicative
import cats.syntax.applicative._

trait PacmanConfig[F[_]] {
  def getBasePackages: F[Set[String]]

  def getCustomPackages: F[Set[String]]
}

class PacmanConfigImpl[F[_]: Applicative] extends PacmanConfig[F] {
  private val LinuxBase = Set("base", "linux", "linux-firmware")
  private val ArchFBase = Set("git", "scala", "sbt", "jdk8-openjdk", "pacman-contrib")
  private val Ethernet = Set("dhcpcd")
  private val MyPackages = Set("vim", "chromium", "xorg-server", "xorg-xinit", "i3-gaps")

  def getBasePackages: F[Set[String]] = (LinuxBase union ArchFBase).pure[F]

  def getCustomPackages: F[Set[String]] = (Ethernet union MyPackages).pure[F]
}

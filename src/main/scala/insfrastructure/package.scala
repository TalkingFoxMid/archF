package object insfrastructure {
  sealed trait ShellCommand
  case class BasicShellCommand(command: String, directory: Option[String] = None) extends ShellCommand
  case class PipeShellCommand(from: ShellCommand, to: ShellCommand) extends ShellCommand

  val yes: ShellCommand = BasicShellCommand("yes")
  implicit class ShellSyntax(private val command: ShellCommand) {
    def |(other: ShellCommand): ShellCommand = PipeShellCommand(command, other)
  }
}

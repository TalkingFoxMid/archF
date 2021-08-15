package object Funcman {
  case class Package(name: String)

  case class FuncmanException(message: String) extends Exception
}

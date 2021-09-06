package object Funcman {
  trait PacmanPackage {
    def name: String
  }
  trait AurPackage {
    def name: String
  }

  case class FuncmanException(message: String) extends Exception
}

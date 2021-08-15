package object Funcman {
  trait VerifiedPackage {
    def name: String
  }

  case class FuncmanException(message: String) extends Exception
}

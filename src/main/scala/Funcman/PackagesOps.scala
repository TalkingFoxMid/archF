package Funcman

object PackagesOps {
  implicit class PacmanStringOps(private val s: String) {
    def asPackages: Set[String] = {
      s.split("\n").toList
        .map(_.split(" ").toList.headOption)
        .collect { case Some(value) => value}
        .toSet
    }

    def asGroup: Set[String] = {
      s.split("\n").toList
        .map(_.split(" ") match {
          case Array(_, b) => Some(b)
          case _ => None
        })
        .collect { case Some(value) => value}
        .toSet
    }
  }
}

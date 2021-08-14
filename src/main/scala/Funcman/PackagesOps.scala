package Funcman

object PackagesOps {
  implicit class AsPackageOps(private val s: String) {
    def asPackages: Set[String] = {
      s.split("\n").toList
        .map(_.split(" ").toList.headOption)
        .collect { case Some(value) => value}
        .toSet
    }
  }
}

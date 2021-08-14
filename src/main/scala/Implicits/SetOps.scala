package Implicits

import cats.syntax.applicative._
import cats.syntax.functor._
import cats.{Applicative, Eval, Traverse}

object CollectionsOps {
  implicit val SetTraverse: Traverse[Set] = new Traverse[Set] {
    override def traverse[G[_], A, B](fa: Set[A])(f: A => G[B])(implicit applicative: Applicative[G]): G[Set[B]] =
      fa.map(f).foldLeft(Set.empty[B].pure[G])((a,b) => applicative.product(a, b).map { case (value, b) => value + b})

    override def foldLeft[A, B](fa: Set[A], b: B)(f: (B, A) => B): B = fa.foldLeft(b)(f)

    override def foldRight[A, B](fa: Set[A], lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B] =
      fa.foldLeft(lb)((a,b) => f(b, a))
  }
}

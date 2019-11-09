/**
The following Scala code is an attempt to see if we can build a type class that encodes aspects of "entropy" in the information-theoretic sense.

It is inspired by the excellent blog post here https://typelevel.org/blog/2018/11/02/semirings.html by Luka Jacobawitz. His shows how Scala types
form a semiring, by starting from a typclass that encodes the cardinality of the number of inhabitants of a particular type. While it may end up
being computationally infeasible to calculate the entropy of a type this way, we'll start by re-creating aspects of his construction here.
*/

// first some imports
import $ivy.`org.typelevel::spire:0.17.0-M1` 
import spire.math._
import spire.implicits._

// now the code

trait Cardinality[A]{
    def cardinality: BigInt
}
object Cardinality {
    def of[A : Cardinality]: BigInt = apply[A].cardinality
    def apply[A : Cardinality]: Cardinality[A] = implicitly

    implicit val cardinalityNothing = new Cardinality[Nothing] {
        def cardinality = BigInt(0) // there are no inhabitants of type Nothing
    }
    implicit val cardinalityUnit = new Cardinality[Unit] {
        def cardinality = BigInt(1) // only one inhabitant of type Unit
    }
    implicit def cardinalityTuple[A : Cardinality, B : Cardinality]: Cardinality[(A,B)] = new Cardinality[(A,B)] { 
        def cardinality = Cardinality[A].cardinality * Cardinality[B].cardinality
    }
    implicit def eitherCardinality[A: Cardinality, B: Cardinality] = new Cardinality[Either[A, B]] {
        def cardinality: BigInt = Cardinality[A].cardinality + Cardinality[B].cardinality
    }
    implicit def functionCardinality[A: Cardinality, B: Cardinality]: Cardinality[A => B] = new Cardinality[A => B] {
        def cardinality: BigInt = Cardinality.of[B].pow(Cardinality.of[A])
    }
    implicit def optionCardinality[A : Cardinality]: Cardinality[Option[A]] = new Cardinality[Option[A]] {
        def cardinality: BigInt = Cardinality.of[A] + BigInt(1)
    }
}

/**
 We didn't give any examples, but suffice to say, the above allows us to calculate the cardinality of most useful types.
 Now, can we do something similar but with Entropy? We shall try.

 First, since Entropy is all about uncertainty, we need a type that represents uncertainty. A fair coin is something which
 comes to mind a good candidate. The type FairCoin has two inhabitants: Heads and Tails. Declaring the type as a
 sealed trait and the classes as final, will (according to the rules of the scala compiler) only allow the values of these
 types to be constructed by actually performing a coin flip!
*/

sealed trait FairCoin
object FairCoin {
    sealed trait Heads extends FairCoin { override def toString = "FairCoin.Heads" }
    sealed trait Tails extends FairCoin { override def toString = "FairCoin.Tails" }

    // naive implementation of a flip method. In a real implementation we would do this differently
    def flip: FairCoin = scala.util.Random.nextBoolean match { case true => new Heads {}; case false => new Tails{} }
}

/**
 Now, let us build an Entropy type class and implement an instance of it for FairCoin
*/

trait Entropy[A] {
    def entropy: BigDecimal //in bits; using BigDecimal so as to have arbitrary precision; there is probably a better way though
}
object Entropy {
    def of[A : Entropy]: BigDecimal = apply[A].entropy
    def apply[A : Entropy]: Entropy[A] = implicitly

    implicit val fairCoinEntropy = new Entropy[FairCoin] {
        def entropy: BigDecimal = BigDecimal(1.0) //a fair coin with 50/50 odds yields 1 bit of entropy
    }
}

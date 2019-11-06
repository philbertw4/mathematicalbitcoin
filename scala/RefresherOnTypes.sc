/**
This document is valid Scala code. Here we will work through a naive implementation of "the category of types and functions."

In Scala we can somewhat encode a (finite) category via a higher kinded type constructor. A category consists of a collection of
objects and morphisms (arrows) between those objects. To some extent, objects only really are used as convenient names for the end points
of morphisms.

There are some laws that a category must abide by. Each object must have an identity morphism, which is a morphism that begins and ends at the
same object. Such a morphism is often called something simple like `id`. Additionally, if the head of one arrow is connected to the tail of another
arrow, then we can infer that there exists a third arrow which goes directly from the tail of the first arrow to the head of the second arrow.

In other words, given A -> B -> C in a category, there is an equivalent arrow A -> C which is the composition of A -> B and B -> C.
*/

trait Category[-->[_,_]] {
    def id[A]: A --> A
    def compose[A,B,C]: (A --> B) => (B --> C) => A --> C
}

object Category {
    def apply[-->[_,_] : Category]: Category[-->] = implicitly

    //a generic class that provides some convenient syntax
    implicit class categorySyntax[-->[_,_] : Category, A, B](ab: A --> B) {
        def andThen[C](bc: B --> C): A --> C = Category[-->].compose(ab)(bc)
        def after[C](ca: C --> A): C --> B = Category[-->].compose(ca)(ab)
    }
}

/**
The morphisms of the category of types and functions are represented by an existing well known Scala trait called Function1[A,B] where A
and B are any two types.
**/
implicit val categoryOfTypesAndFunctions = new Category[Function1]{
    def id[A]: A => A = identity
    def compose[A,B,C]: (A => B) => (B => C) => A => C = f => g => f andThen g
}

import Category._

val Void = Category[Function1].id[Nothing]

val Unit = Category[Function1].id[Unit]

val True: Unit => Boolean = (u: Unit) => true

val False: Unit => Boolean = (u: Unit) => false

val Not: Boolean => Boolean = b => b match { case true => false; case false => true }

//demonstrating some different syntax
val notTrue1 = True andThen Not   // Unit => false
val notTrue2 = Not after True     // Unit => false



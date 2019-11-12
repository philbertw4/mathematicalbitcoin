## A category called Git?

Category theory can be an interesting tool to use when thinking about abstract things. This is not surprising,
since category theory itself is considerd quite abstract. However, understanding bits and pieces
of category theory, sometimes helps to see semblances of connections between otherwise disparate patterns.

Here we will gloss over the definitions we need in hopes that at some point they may be filled in later
(when the author has more time). Instead we will simply pose some questions, along with some partial answers to those questions.

If we are thinking about git in terms of category theory, we need to ask ourselves, what is the category called? For simplicity, we will call the category (if it exists -- which we don't know whether it does yet) Git.

Categories have zero or more objects and zero or more morphisms between any two objects. What makes category theory interesting is that if we can show that the objects and morphisms meet certain criteria, we may get a lot of mathematical information essentially "for free." Free is relative here, since, of course it takes a fair amount of mental work to slog through this.

Figuring out what objects exist in a category is important and determining how those objects relate to other objects is also crucially important. For our purposes this simply means, if we can discover certain things about our category Git, then we might be able to obtain some convenient algebraic notions and notations.

# Our Objects and Morphisms
For this investigation we will assume that the objects of our category are valid git repositories. A repository is
comprised of (potentially infinite) sequence of commits, but that is of less importance to us here. What is important to us that that the git protocol allows us to perform some operations on repositories such that we end up with a new repository.

The morphisms in our category will be the various operations available to us when using git.

# Pairing Objects

Often it is convenient when working in a category to know whether we can create a new object which is simply the pair (also known as product) of two objects. So, for two objects `a` and `b` in our category C, we can form a pair which we can denote `(a,b)` or sometimes `a*b`. This new object `a*b` is in fact also an object in our category. We have simply given it the convenient name of `a*b`.
Can we use git to pair two repositories and obtain a third repository such that a*b = c = b*a for repositories a, b, and c? According to [this blog post](https://saintgimp.org/2013/01/22/merging-two-git-repositories-into-one-repository-without-losing-file-history/) the answer seems to be yes.

# Terminal Object
Can an empty git repository serve as a terminal object? Let's let `1` denote the canonical empty git repository. For a given repository `a` what happens when we combine it with `1`? Well, `1` is the empty repository, so we do not gain any information, and therefore `a*1 = a`. So far so good!

# Initial Object
Constructing an initial object for the category `Git` may be more difficult. Taking a clue from the initial object for `Set` the iniital object is the empty set. The morphisms of `Set` are functions. Can we construct a function from the empty set to any other set? Being able to accomplish this is part of the universal property of the initial object of a category. 

Sure, we can define such a function! In mathematics functions just exist, there is no need to worry about how to compute the output of a function from a given input -- it can simply be defined. However, in programming things are different. We want to know how to compute things. In the category of types and functions (which is eerily similar to the category `Set`), the initial object, in Scala, is known as `Nothing`. The type `Nothing` has no inhabitants, sort of how the empty set has no elements. One of the properties of being an initial object of a category is that there exists a unique morphism from the initial object to any other object in the category. In scala, we can construct such a morphism. It is called `absurd` and its definition looks something like this `def absurd[A](n: Nothing): A`. This is essentially tricking the compiler into saying, "if you give me an element of type Nothing," then I will happily give you any object (e.g. type) of a type `A` which you so desire. However, because `Nothing` is a type with not inhabitants, it is actually impossible to call such a function at runtime.

Can we come up with a similar sort of "absurd" operation in the world of Git? If so, this may provide us with a way to identify whether the category `Git` has an initial object. So, let's break this down a bit further. In the category `Git`, the objects are repositories and the morphisms are git operations on those repositories (thereby yielding another, or possibly the same, repository, depending on the operation). Repositories individually represent a "history" in the form of commits which were made to the repository. 

Here is one potential candidate: a git repository with a single commit object which commits to an obviously extremely-difficult-to-produce history.
According to [this stackoverflow question](https://stackoverflow.com/questions/16064968/building-git-commit-objects-with-git-hash-object), along with a quick chat on the #git IRC channel, it does in fact seem possible to create a commit object by hand. As such, it may be possible to construct a git repository which, is impossible (due to computational in-feasibility) to "pair" with any other repository. 

If we name our initial repository `0`, then we can think about using `0` with our pairing operation on repositories. For a given repository `A`, what is a realistic notion of `(A,0)` or `(0,A)`? Given that `0` is a repository with a history which is nearly impossible to construct, then we will have a hard time actually completing the pairing operation. In fact, since it is so computationally difficult to calculate the history of `0`, the end of the universe might arrive before we are finished doing so. Hence, for all practical means and pruposes, `0` annhilates anything it is paird with. Therefore `(A,0) = (0,A) = 0`. This is a nice property to have.

# Exponential Object
In the category `Set` and also the category of types, there is an exponential object `a => b` for objects `a` and `b`. Intuitively this object represents a (total) function from object `a` to type `b` in the category `Set`. It is often more convenient, and visually appealing, to write the exponential object as `b^a`, which, if it were natural numbers we are talking about, is `b` raised to the power of `a`. Additionally, such a notation reminds us that, in many cases, there is a deep relationship between exponentiation and repeated multiplication.

What is the natural notion of a function object, or an exponential object, in the context of `Git`? That is the question we seek to answer. In order to do so, it might be helpful to think about how function objects work in the category `Scala` (which is shorthand for the category of scala types and functions between types -- if we were talking about Haskell, we might call the category `Hask`). In the category `Scala`, how many functions are there of type `Boolean => Int`? Well, there are two inhabitants of `Boolean`, namely `true` and `false`. The function returns an `Integer`, but the value of that integer may be different depeneding on whether the value of the input (a `Boolean`) is `true` or `false`. So, if the input is `true`, then the number of possible inhabitants of type `Boolean => Int` is the same as the number of inhabitants of type `Int`. The number of inhabitants of type `Int` is implementation independent, but for our purposes, we can think about it mathematically and remember that the set of integers is of infinite size. Now, if the input is `false` we can follow the same logic. Ultimatley, what we come to realize is that the number of inhabitants of type `Boolean => Int` is the same as the number of inhabitants of type `(Int,Int)` which is just our aforementioned product type `(A,B)` where both `A` and `B` are of type `Int`. As such, there are `|A|*|B|` inhabitants of such the product type where `|A|` means "number of inhabitants of type `A`. Since the number of inhabitants of `Int` is unbounded, then there are an infinite number of functions of type `Boolean => Int`.

What about the other way around? How many inhabitants of the type `Int => Boolean` are there? We can think of functions which return a boolean value as predicates in a logical sense. They take one value, in this case of type `Int` and return another value which is either `true` or `false`. An example of a predicate is the function `isPrime: Int => Boolean` to which we can give an integer and it will tell us whether the integer is prime. So, if types of the form `A => Boolean` are predicates on the type `A`, how many such predicates are there? There are `|Boolean|^|Int| = 2^infinity` predicates.

Unfortuntately, when the number of inhabitants of a type is infinite, it is difficult to reason about statements involving that type. This is one reason why we often restrict ourselves to talking about "finite types," and for similar reasons mathematicians often talk about "finite sets."

# An Algebra of Git?

 



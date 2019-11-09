## A category called Git?

Category theory can be an interesting tool to use when thinking about abstract things. This is not suprising,
since category theory itself is considerd quite abstract. However, understanding bits and pieces
of category theory, sometimes helps to see semblances of connections between otherwise disparate patterns.

Here we will gloss over the definitions we need in hopes that at some point they may be filled in later
(when the author has more time). Instead we will simply pose some questions, along with some partial answers to those questions.

If we are thinking about git in terms of category theory, we need to ask ourselves, what is the category called? For simplicity, we will call the category (if it exists -- which we don't know whether it does yet) Git.

Categories have zero or more objects and zero or more morphisms between any two objects. What makes category theory interesting is that if we can show that the objects and morphisms meet certain criteria, we may get a lot of mathematical information essentially "for free." Free is relative here, since, of course it takes a fair amount of mental work to slog through this.

Figuring out what objects exist in a category is important and determining how those objects relate to other objects is also crucially important. For our purposes this simply means, if we can disover certain things about our category Git, then we might be able to obtain some convenient algebraic notions and notations.

# Our Objects and Morphisms
For this investigation we will assume that the objects of our category are valid git repositories. A repository is
comprised of (potentially infinite) sequence of commits, but that is of less importance to us here. What is important to us that that the git protocol allows us to perform some operations on repositories such that we end up with a new repository.

The morphisms in our category will be the various operations available to us when using git.

# Pairing Objects

Often it is convenient when working in a category to know whether we can create a new object which is simply the pair (also known as product) of two objects. So, for two objects `a` and `b` in our category C, we can form a pair which we can denote `(a,b)` or sometimes `a*b`. This new object `a*b` is in fact also an object in our category. We have simply given it the convenient name of `a*b`.
Can we use git to pair two repositories and obtain a third repository such that a*b = c = b*a for repositories a, b, and c? According to [this blog post](https://saintgimp.org/2013/01/22/merging-two-git-repositories-into-one-repository-without-losing-file-history/) the answer seems to be yes.

# Terminal Object
Can an empty git repository serve as a terminal object?

# Initial Object
Can a git repository with a single commit object which commits to an obviously extremely-difficult-to-produce history serve as an initial object?
According to [this stackoverflow question](https://stackoverflow.com/questions/16064968/building-git-commit-objects-with-git-hash-object), along with a quick chat on the #git irc channel, it does in fact seem possible to create a commit object by hand. As such, it may be possible to construct a git repository which, is impossible (due to computational infeasibility) to "pair" with any other repository. This impossibility gives us the mathematical notion of anhilation. This is a nice property to have, as it might open the door to the following types of "operations":

We will call our initial object `0` and our terminal object `1`. Can we create a repository that is the pair (0,1)? Sure we can, but we would need to know what the history of `0` is in order to do the combining. We could be waiting quite a while! Therefore, `(0,1) = 0*1 = 0`. Great!

For a given repository `a` what happens when we combine it with `1`? Well, `1` is the empty repository, so we do not gain any information, and therefore `a*1 = a`. So far so good!

 



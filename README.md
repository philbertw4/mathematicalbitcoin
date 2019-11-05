# Mathematical Bitcoin and Bitcoinical Mathematics
We have mathematical physics, mathematical philosophy, and other "mathematical _____" subjects of study, so why not Mathematical Bitcoin?
The purpose of this repository is to attempt to develop bitcoin from first principles using concepts from mathematics. Perhaps once/if this is achieved, then there can be another body of work that begins developing which we might dub "Bitcoinical Mathematics." The former (Mathematical Bitcoin) is
using math to study or otherwise "speak" the language of bitcoin, if such a language exists. The latter is the study of actually performing/doing/creating math within the context of bitcoin itself. 

## Is there a Bitcoin Monad?
If you do not know what a monad is, that's ok. One goal here is to determine whether such a bitcoin monad exists and whether it would be useful to programmers.

A monad is something that comes from the mathematical realm of category theory, but the concept has made inroads into many existing programming
languages. Ultimately, at least from a programming perspective, a monad provides a relatively clean way to encapsulate sequential computation or processes.

There is a chance of course that the mathematical object which we seek is not a monad but instead something else. Monad is just a placeholder right now. It has been joked that "a monad is just a monoid in the category of endofunctors," well for all we know, perhaps a statement like "bitcoin is just a semiring in the category of endofunctors" is a statement that ends up being more realistic statement. Who knows?

## What does this have to do with bitcoin?
Probably more than we currently realize. However, the nice thing about a monad is that, for it to be a valid monad, it must obey some laws called
"the monad laws." When something obeys these laws, then we can more safely build systems on top of, or otherwise utilize, that monadic abstraction.

## Progress Updates
1. Oct 19, 2019 -- included bitcoin-s library and a scala worksheet (depends on ammonite being installed) to play with manual construction of transactions and such.

2. Nov 4, 2019 -- some musings about [bitcoin space and time, renting versus buying](blob/master/RentingVsBuying.md) 
## Todo
1. Document build instructions (including ammonite and mill for building scala)

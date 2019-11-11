# Mathematical Bitcoin and Bitcoinical Mathematics
We have mathematical physics, mathematical philosophy, and other "mathematical _____" subjects of study, so why not Mathematical Bitcoin?
The purpose of this repository is to attempt to develop bitcoin from first principles using concepts from mathematics. Perhaps once/if this is achieved, then there can be another body of work that begins developing which we might dub "Bitcoinical Mathematics." The former (Mathematical Bitcoin) is
using math to study or otherwise "speak" the language of bitcoin, if such a language exists. The latter is the study of actually performing/doing/creating/discovering math within the context of bitcoin itself. 

## Disclaimer
This is currently mostly just a playground for one author to muse about fusing together aspects of mathematics and Bitcoin. As such, it is not even remotely "formal enough" to pass mathematical scrutiny. Nor is it "cypherpunk enough" to be useful for bitcoin. Hopefully, in time, both of those things will be remedied. However, for now, these musings are simply half baked and will likely remain so for quite some time as the author grapples to better understand both mathematics and bitcoin.

## Is there a Bitcoin ~~Monad~~ Operad?
If you do not know what a monad or an operad is, that's ok. One goal here is to determine whether such a mathematical object exists in the context of bitcoin, and whether it would be useful to programmers, users, and the greater ecosystem.

A monad is something that comes from the mathematical realm of category theory, but the concept has made appearances in many existing programming
languages. Ultimately, at least from a programming perspective, a monad provides a relatively clean way to encapsulate sequential computation or processes.

There is a chance of course that the mathematical object which we seek is not a monad but instead something else. Monad is just a placeholder right now. For example, it may turn out 
that the mathematical notion of an operad is actually a better description for Bitcoin. It has been joked that "a monad is just a monoid in the category of endofunctors," well for all we know, perhaps a statement like "bitcoin is just a semiring in the category of endofunctors" is a statement that ends up being more realistic statement. Who knows?

One reason why an operad may be a good fit as a mathematical model of Bitcoin comes from the fact that a good portion of Bitcoin's state is given by something known as the UTXO set. UTXOs are also known colloquially as "outputs." A Bitcoin transaction takes inputs and, unsurprisingly, produces outputs. An output is subsequently used as the input in another transaction.

Similar to monads, operads can help model the idea of gluing things (computations) together. In fact, operads greatly generalize monads. One operation that is difficult to do monadically, but may be (slightly) easier to do operadically, has to do with linking Bitcoin transactions together by their indexes. Operads come equipped with an algebra for doing precisely this.

## What does this have to do with Bitcoin?
Being able to describe bitcoin in purely abstract mathematical terms may provide some fresh insights. Of course, there are many other mathematical models which have been put forth by the academic and bitcoin community. However, most of those models are purpose built for doing things like analyzing incentives of mining, or determining properties of the network. Instead, what we are working on here is an abstract mathematical model that may facilitate high level, yet consistent-across-implementations, methods of interacting with the Bitcoin network. In other words, interacting with the model that is (hopefully, eventually) developed here will also entail interaction with the bitcoin network directly.

## Progress Updates
1. Oct 19, 2019 -- included bitcoin-s library and a scala worksheet (depends on ammonite being installed) to play with manual construction of transactions and such.

2. Nov 4, 2019 -- some musings about [bitcoin space and time, renting versus buying](RentingVsBuying.md) 

3. Nov 5, 2019 -- refresher about [types and functions in programming languages](scala/RefresherOnTypes.sc)

4. Nov 7, 2019 -- some [typclassery around the notion of information-theoretic entropy](scala/EntropyTypeclass.sc)

5. Nov 8, 2019 -- [is Git a Category?](CategoricalGit.md). Updated Nov 9, 2019.

6. Nov 10, 2019 -- Updated Readme

## Ideas for Next Update
1. Keep working through the categorical aspects of Git, trying to uncover any deep connections between the Git protcol and the Bitcoin protocol.
2. Learn more about manually constructing bitcoin and lightning transactions

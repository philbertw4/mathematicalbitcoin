# The Bitcoin Monad
If you do not know what a monad is, that's ok. The purpose of this repository is to track some efforts in uncovering whether
such a bitcoin monad exists and whether it would be useful to programmers.

A monad is something that comes from the mathematical realm of category theory, but the concept has made inroads into many existing programming
languages. Ultimately, at least from a programming perspective, a monad provides a relatively clean way to encapsulate sequential computation or processes.

## What does this have to do with bitcoin?
Probably more than we currently realize. However, the nice thing about a monad is that, for it to be a valid monad, it must obey some laws called
"the monad laws." When something obeys these laws, then we can more safely build systems on top of, or otherwise utilize, that monadic abstraction.

## Progress Updates
1. Oct 19, 2019 -- included bitcoin-s library and a scala worksheet (depends on ammonite being installed) to play with manual construction of transactions and such.

## Todo
1. Document build instructions (including ammonite and mill for building scala)

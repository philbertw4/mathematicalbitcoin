## In the land of bitcoin, is it cheaper to rent, or to buy?

Buying space in the Bitcoin blockchain is a somewhat controversial topic. Some people believe may that Bitcoin's blockchain should not be used to store any arbitrary data whatsoever. Others may believe that the only ultimate purpose of Bitcoin is precisely to transport seemingly arbitrary data forward into the future, and that it is the future which will ultimately decide whether that seemingly arbitrary data is meaningless and useless. While it may not be possible to know a priori which viewpoint will ultimately prove to be the "correct" one, the fact of the matter is that the bitcoin blockchain does in fact utilize some of its precious block space to propagate data forward through time.

In fact, early in the life of Bitcoin people began embedding data directly into the blockchain. Some examples are obituaries, chats, transcripts, etc. Since data in a block is kept by full nodes for all of time, it is tempting to use them as a mechasim to store your data. There are a few primary ways this can be done and each of them has some different tradeoffs and costs. Two of the primary ways are quickly explained below.

### Buy with P2SH

P2SH is a mechanism that was added to bitcoin whereby a valid UTXO is a hash of a script rather than the script itself. While of course P2SH is used for many legitmate purposes, one consequence of its design is that it is possible to create a UTXO where the spending script is unknowable, not even by the person who created it. However, this unknowability, due to the properties of the hash function used is extremely difficult to prove. Therefore full nodes must error on the side of caution and assume that the UTXO is in fact spendable. As such, they will hang on to the data, potentially indefinitely.

### Buy with `OP_RETURN`

`OP_RETURN` was created partly to combat the growing use of P2SH addresses which were obviosly fake. When an address, which is usually just a long string of random letters, contains multiple human-recognizable pattern that cannot easily be explained by in-this-universe chance, then it is reasonable to assume that the script which spends that UTXO is unknowable. `OP_RETURN` is a simple instruction which, when encountered as part of a spending script, instructs full nodes to mark the transaction as invalid. In other words, satoshis sent to a UTXO which is encumbered by an `OP_RETURN` are unspendable. From a full node's perspective, however, the benefit of `OP_RETURN` over P2SH is that the node can safely disregard the data associated with that UTXO.

Both P2SH and `OP_RETURN` amount to "buying" space in the Bitcoin blockchain. The price of the space is denominated in satoshis/byte (reminder: 1 BTC = 100,000,000 satoshis). There are some sublties here which we are glossing over (such as the rate being different depending on the method chosen), but the important thing is that one can in fact buy precious space. See [here](https://learnmeabitcoin.com/guide/nulldata) for more details.

### Rent with ???

Buying is permanent, or at least perceived to be permanent. Space you buy on bitcoin's blockchain will likely still exist and be held by you (or in memory of you) for as long as bitcoin exists. What if you only want some data stored on the chain temporarily? Does a mechanism exist to accomplish such a thing? 

//TODO: work through known mechaisms for renting space on bitcoin's blockchain

### It's About Time

Above we have established that it is possible to either rent or buy space in the bitcoin blockchain. Can the prices of the two mechanisms be compared so as to impute some sort of implicit time preference?



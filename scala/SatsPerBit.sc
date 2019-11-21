/**
Here we will attempt to do a simple derivation around the conversion from energy to satoshis and back
**/

// first some imports that will help us to do math with large numbers
import $ivy.`org.typelevel::spire:0.17.0-M1` 
import spire.math._
import spire.implicits._

/**
  Bitcoin's current target is a 256 bit number and can be calculated from the current difficulty.
  Difficulty = Max_Target / Current_target which implies that Current_target = Max_Target / Difficulty
*/

// as of block #602,784 the current difficulty is 12720005267390, and will remain so for the next 2016 blocks
val difficulty = BigInt("12720005267390")

// approximating max target as 2^224 per https://bitcoin.stackexchange.com/questions/8806/what-is-difficulty-and-how-it-relates-to-target
// why this is not 2^256 is because the lowest difficulty was based on iterating through 2^32 hashes
val max_target = BigInt(2).pow(224)

val current_target = max_target / difficulty

// it takes, on average, 2^32 * difficulty hashes to find a block
val num_hashes = difficulty * BigInt(2).pow(32)

/**
  num_hashes is a good start towards an estimate of time, but what we are really after is something more general
**/

// let p be the probability of flipping heads on this extremely biased hash coin
// since the output of the hash function is approximately uniformly distributed
// calculating p is straightforward:
val p = BigDecimal(current_target) / BigDecimal(max_target)

// let w be the entropy (in bits) pertaining to the number of failures (hashes) until the first success
// this is a geometric distribution
val w = (-(BigDecimal(1) - p)*log(BigDecimal(1)-p,2) - p*log(p,2)) / p
// this yields w = 44.974859 bits

// clearly w is a function of p

/**
 It might be interesting to do this same calculation for each block in the history of bitcoin. That's quite a bit of data, as there are over 600,000 blocks!
 For each of these blocks we can calculate the minimum amount of work that was performed. Work in this case is defined as the number of trials that were expected
 to have been attempted before the first successful block was found.

 Incidentally, the geometric distribution is the only discrete memoryless distribution. Since the distribution is memoryless, we might be able to sum the minimum
 work of each block together so as to get a minimum bound on the work of the entire sequence. It is a minimum bound because, naturally, there are other
 constraints which go into building a block (for example, a block must have a valid timestamp, etc). While it may be possible to also account for these additional
 constraints in an information-theoretic way, the proof of work constraint, due to its massive number of iterations, might dominate.

 Anyway, back to our value of w. With w = 44.97 bits/block (at current block height), we are basically saying that, in expecation, a minimum of 44.97 bits of work have been performed
 to create that block. Now, interestingly, the size of a block itself can also be measured in terms of bits. Are they the same bits as those of the bits of work? At this point in
 our analysis we do not know. Nevertheless, if we consider our block to be a "message" we can easily calculate a ratio of bits of work per bit of message.

 Additionally, we can begin to start thinking about these concepts in terms of a coordinate system.

 w |
   |
   |
   |
   |_____________
                x

Here x is the position in the message. In this case our message is a block and we can model it as a line segment between two points. The values of the points
along the line segment are the bit values of the message (e.g. 1 or 0) itself. This is not the only way to interpret this. For example, we might instead utilize a point (x,w,b) where x is the size of the block, w is the work of the block, and b is the block itself. Each of x and b are drawn from the set of natural numbers. However, w is from the set of real numbers.

Let's build some definitions/functions, rather than just value calculations which may be helpful if we want to calculate these numbers for the existing bitcoin blockchain.

Becuase it takes, on average, 2^32*Difficulty hashes to finde a block, we can calculate the work of a block directly from the difficulty by first calculating p.
**/

def work(difficulty: BigInt): BigDecimal = {
    // return right away if difficulty is 1, since, this gives p = 1 which yields entropy of 0
    if(difficulty == BigInt(1)) return BigDecimal(0.0)

    // why this is not 2^256 is because the lowest difficulty was based on iterating through 2^32 hashes
    val max_target = BigInt(2).pow(224)

    val current_target = max_target / difficulty
    
    // let p be the probability of flipping heads on this extremely biased hash coin
    // since the output of the hash function is approximately uniformly distributed
    // calculating p is straightforward:
    val p = BigDecimal(current_target) / BigDecimal(max_target)
 
    // let w be the entropy (in bits) pertaining to the number of failures (hashes) until the first success
    // this is a geometric distribution
    val w = (-(BigDecimal(1) - p)*log(BigDecimal(1)-p,2) - p*log(p,2)) / p
    w
}

import ammonite.ops._
def difficultySequence = read.lines! pwd/'scala/"btcHistoricalDifficulty.csv" map(_.split(",")(2)) drop(1) map(BigInt(_))

def accumWork = difficultySequence.map(work(_)).sum
//res12: BigDecimal = 8724.803126658363972903393740660985



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

def calcWork(difficulty: BigInt): BigDecimal = {
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
//read difficulty as a BigInts from a csv file
val readdifficultySequenceFromFile = read.lines! pwd/"btcHistoricalDifficulty.csv" map(_.split(",")(2)) drop(1) map(BigInt(_))

def numHashesSequence = readdifficultySequenceFromFile.map(_ * BigInt(2).pow(32) * BigInt(2016))
def accumNumHashesSequenceLog2 = (numHashesSequence.scanLeft(BigInt(1)){case (tot: BigInt, hashes: BigInt) => tot + hashes}).map(n => BigDecimal(n).log(2))

//turn difficulty into work and keep a running total
def workSequence = readdifficultySequenceFromFile map(calcWork(_)) map(_ * BigDecimal(2016))
def accumWorkSequence = workSequence.scanLeft(BigDecimal(0.0)){case (tot:BigDecimal, diff:BigDecimal) => tot + diff}

def accumWork = accumWorkSequence.last
//res12: BigDecimal = 8724.803126658363972903393740660985

def writeCsv = write(pwd/"accumWork.csv",(readdifficultySequenceFromFile zip accumNumHashesSequenceLog2 zip accumWorkSequence zipWithIndex).map{case(((d,h),w),i) => s"$i,$d,$h,$w\n"})

//also, for reference, an old stack exchange answer: https://bitcoin.stackexchange.com/questions/26869/what-is-chainwork
// however, the answer does not translate work all the way down into an independent representation of bits
// instead they stop short and talk only about number of hashes

/**
 If we can generalize the calculations above such that they can be done for any proof of work function, then we may be able to do some interesting things. Since the networks are different networks at the moment and do not necessarily share cryptographic hash functions, there may be one network (likely bitcoin) the network currency of which commands more work. Therefore, rather than work on automic swaps between networks and such, the largest network could instead simply offer to pay for the smaller network's work. 

Using bitcoin as an example, the lightning network (or a dedicated public channel on it?) might be able to pay some amount of satoshis per bit of work. Naturally in order to verify the work, and hence be comfortable issuing payment, the work must be directed toward the most recent block header. So, in part, this is a way for bitcoin to "hire away" miners who are optimized for other proof of work algorithms and still reward them fairly according to the actual work they produce (e.g. bits of work, not simply hashes). Such a mechanism then opens the doors for bitcoin (or a layer on top of bitcoin like lightning) to be able to accept and pay for *any* proof of work, not simply sha256.
**/


def heightWithRewardWithWork = (workSequence zipWithIndex) map {case (w,i) => (i*2016,w)} map {case (b,w) => (b,BigDecimal(50.0) / BigDecimal(2).pow((b / 210000)),w)}

/**
  heightWithRewardWithWork.filter(_._3 > BigDecimal(0.0)).map{case (b,r,w) => (b,r*BigDecimal(100000000) / w)} 
res18: IndexedSeq[(Int, BigDecimal)] = ArraySeq(
  (38304, 2500000000.000000000000000000000001),
  (40320, 1814956144.696797771797713298515522),
  (42336, 1540778633.509139084521994822198782),
  (44352, 1540778633.509139084521994822198782),
  (46368, 1282007059.418245266979983105873054),
  (48384, 1207230990.070512633302675781904222),
  (50400, 1034240206.581049081375174059258474),
  (52416, 1006886660.997351721483688093172313),
  (54432, 1034240206.581049081375174059258474),
  (56448, 926502233.3279606975905458701989304),
  (58464, 911266632.5261823315966575263428562),
  (60480, 884646872.7516237450502135248187980),
...
**/






/**
Here we will attempt to do a simple derivation around the conversion from bits of work to satoshis and back. Usually
the accumulated work attributed to the main chain of bitcoin is measured in terms of expected number hashes. However,
here we will step down further into more fundamental territory and reinterpret this work in terms of the expected minimum
number of information-theoretic bits of work which were performed. 

By reinterpreting the bitcoin blockchain (or at least the proof-of-work aspects of the blockchain) in this manner we seek
to establish a lower bound on the complexity of the bitcoin blockchain. In more straighforward terms, this amounts to making the following statement:

The bitcoin blockchain cannot be faithfully described using fewer than approximately 17.5 million bits.

Of course, we paln to generalize the analysis peformed here such that it is applicable to any proof-of-work system irrespective of its protocol parameters such as chosen hash function, target block time, etc. For simplicity, however,
the following analysis will be restricted first to bitcoin only.

First, a quick note on this document:
  * this document is writen in essentially literate Scala
  * the Ammonite Scala REPL can be used to run this file by performing the following steps
  * 1. install Ammonite https://ammonite.io/
  * 2. clone this repository
  * 3. open a REPL session by `amm`
  * 4. you now have a Read Eval Print Loop open and can follow along with the remainder of this document

*/

/*
Let us import some libraries that will help us to do math with large numbers
the calculations we do below are by no means optimized for efficiency, but we intend them to be accurate by utilizing arbitrary precision datatypes where appropriate.
*/
import $ivy.`org.typelevel::spire:0.17.0-M1` 
import spire.math._
import spire.implicits._

/**
  Bitcoin's current target is a 256 bit number and can be calculated from the current difficulty.
  Difficulty = Max_Target / Current_target which implies that Current_target = Max_Target / Difficulty

  As of block #602,784 the current difficulty is 12720005267390, and will remain so for the next 2016 blocks
*/

object Example {

    val current_difficulty = BigInt("12720005267390")

    // approximating max target as 2^224 per https://bitcoin.stackexchange.com/questions/8806/what-is-difficulty-and-how-it-relates-to-target
    // why this is not 2^256 is because the lowest difficulty (a difficulty value of 1) was based on iterating through 2^32 hashes
    val max_target = BigInt(2).pow(224)

    val current_target = max_target / current_difficulty

    // The number of hashes, in expecation, it takes to find a block is given by 2^32 * current_difficulty.
    val num_hashes = current_difficulty * BigInt(2).pow(32)

    /**
      While num_hashes is a good start towards an estimate of time, but what we are really after is something more general.

      There is a probability p = current_target / 2^256 that a flip of this "hash coin" will result in a number h such that 
      h is less than or equal to current_target. This is predicated on the assumption that the output of the hash function 
      approximates a uniform distribution.

      Calculating p is straightforward:
    */
    val p = BigDecimal(current_target) / BigDecimal(2.0).pow(256)

    // Now, let w be the entropy (in bits) pertaining to the number of failures (hashes) until the first success
    // This is a geometric distribution (https://en.wikipedia.org/wiki/Geometric_distribution)
    val w = (-(BigDecimal(1) - p)*log(BigDecimal(1)-p,2) - p*log(p,2)) / p
    // Performing the calculation, we get w = 76.9748595424 bits of work.
}

/* Clearly w is a function of p and p is a function of difficulty. So, at least for bitcoin, we can create a simple function
   which performs the above steps for any difficulty it is given. Note, of course, that this function simply calculates what
   the expected minimum work must be in order to achieve or exceed the desired difficulty.
*/

def calcMinimumBitcoinWork(difficulty: BigInt): BigDecimal = {
    // return right away if difficulty is 1, since, this gives p = 1 which yields entropy of 0
    //if(difficulty == BigInt(1)) return BigDecimal(0.0)

    // why this is not 2^256 is because the lowest difficulty was based on iterating through 2^32 hashes
    // max target = (2^16 - 1) * 2^208
    val max_target = (BigInt(2).pow(16)-1)*BigInt(2).pow(208)

    val current_target = max_target / difficulty
    
    // let p be the probability of flipping heads on this extremely biased hash coin
    // since the output of the hash function is approximately uniformly distributed
    // calculating p is straightforward:
    val p = BigDecimal(current_target) / BigDecimal(2.0).pow(256)
 
    // let w be the entropy (in bits) pertaining to the number of failures (hashes) until the first success
    // this is a geometric distribution
    val w = (-(BigDecimal(1) - p)*log(BigDecimal(1)-p,2) - p*log(p,2)) / p

    // now we return w
    w
}

/**
 The above function calculates the minimum work because, naturally, there are other protocol-specific constraints which go into building 
 a block (for example, a block must have a valid timestamp, transactions must be valid, etc). While it may be possible to also account for
 these additional constraints in a similarly information-theoretic way, the proof of work constraint, due to its massive number of iterations, 
 might dominate. For now we will assume that to be the case. Nevertheless, this may not be a wise assumption.

 Now back to our value of w. With w = 76.97 bits/block (at the example block height of 602,784), we are basically saying that, in expecation, 
 no fewer than 44.97 bits of work have been performed to create that block. Now, interestingly, the size of a block itself can also be measured 
 in terms of bits. Are they the same bits as those of the bits of work? At this point in our analysis we do not know. Nevertheless, if we 
 consider our block to be a "message" we can easily calculate a ratio of bits of work per bit of message.

 For sake of completeness, let us now calculate how the number of bits of work has accumulated over the history of bitcoin's blocks. We can
 do this by taking the difficulty for each difficulty period in bitcoin's history and using our calcMinimumBitcoinWork() function.
**/

// Let us bring in a library which lets us load a file and read its lines. The data file is included in the working directory in the git repository.
import ammonite.ops._

//Next, we read difficulty from the file and convert it to a BigInt. 
val readdifficultySequenceFromFile: IndexedSeq[BigInt] = read.lines! pwd/"btcHistoricalDifficulty.csv" map(_.split(",")(2)) drop(1) map(BigInt(_))

//Then difficulty into work and multiply by 2016 since there are 2016 blocks in each difficulty period.
def workSequence = readdifficultySequenceFromFile map(calcMinimumBitcoinWork(_)) map(_ * BigDecimal(2016))

//Finally, we take the sequence of work and calculate a running total. 
def accumWorkSequence = (workSequence.scanLeft(BigDecimal(0.0)){case (tot:BigDecimal, diff:BigDecimal) => tot + diff}).drop(1)

//Now, stopping for a moment to check our work, if we take look at the last value of the accumWorkSequence
def accumulatedWork = accumWorkSequence.last
/** 
 This gives us 36937050.5958 bits of accumulated work. This is approximately 36.9 million bits or approximately 4.62 megabytes.

 Can we safely interpret this to mean that it is fundamentally impossible to faithfully encode the bitcoin blockchain in fewer 
 than 4.62 megabytes? It seems so.
*/


/* Moving on, it may be helpful to perform these calculations and compare the results to how bitcoin currently measures its 
accumulated work (e.g accumulated number of hashes rather than bits of work). Here we calculate the base 2 logarithm of the accumulated 
number of hashes.
*/
def numHashesSequence = readdifficultySequenceFromFile.map(_ * BigInt(2).pow(32) * BigInt(2016))
def accumNumHashesSequenceLog2 = (numHashesSequence.scanLeft(BigInt(1)){case (tot: BigInt, hashes: BigInt) => tot + hashes}).drop(1).map(n => BigDecimal(n).log(2))

def writeCsv = write(pwd/"accumWork.csv",(readdifficultySequenceFromFile zip accumNumHashesSequenceLog2 zip accumWorkSequence zipWithIndex).map{case(((d,h),w),i) => s"${(i+1)*2016},$d,$h,$w\n"})
/** this output is also available in the git repository as accumWork.csv
    height,difficulty,accum_hashes_log2,accum_work_bits
2016,1,42.977279923500083089206077365700758589,67420.47320209355938532518718091770
4032,1,43.977279923499999779777762287819491647,134840.9464041871187706503743618354
6048,1,44.562242424221128191422062871404494537,202261.4196062806781559755615427531
8064,1,44.977279923499958125063604747074822850,269681.8928083742375413007487236708
10080,1,45.299208018387312141991092668270956441,337102.3660104677969266259359045885
12096,1,45.562242424221100421612624510507151935,404522.8392125613563119511230855062
14112,1,45.784634845557544380485220832190767986,471943.3124146549156972763102664239
... elided ...
586656,9013786945891,90.9264732773219864879254577601757991801,35697342.24031584069001596346930766
588672,9985348008059,90.9785366611035809193225468656033477775,35851819.54041148534029336818675744
590688,10183488432890,91.0297660757473525573553862288282640579,36006353.98853929175922931420431344
592704,10771996663680,91.0820464050887502112008691591733183613,36161051.84119982318256318088458699
594720,11890594958795,91.1376376368821511022585479594365420805,36316037.04485496816936039050908960
596736,12759819404408,91.1950022344839190516935111145453688531,36471227.45111503699703993328751778
598752,13008091666971,91.2512256772816108744332475045157418729,36626473.90503579497082155154529624
600768,13691480038694,91.3081275449810860860464939716200853658,36781869.27902214368464836737095923
602784,12720005267390,91.3590535150456863178396562084687568224,36937050.59585981672351616433255982

*/

/**
 If we can generalize the calculations above such that they can be done for any proof of work function, then we may be able to do some interesting
 things. Since the networks are different networks at the moment and do not necessarily share cryptographic hash functions, there may be one 
 network (likely bitcoin) the network currency of which commands more work. The largest network could instead simply pay for the work capabilities 
 of the smaller network to be redirected towards working on the larger one.  

 For example, bitcoin might be able to pay some amount of satoshis per bit of work for any hash function. Naturally, in order to verify the work, 
 and  hence be comfortable issuing payment, the work must be directed toward the most recent block header. So, in part, this is a way for bitcoin 
 to "hire away" miners who are optimized for other proof of work algorithms and still reward them fairly according to the actual work they produce
 (e.g. bits of work, not simply hashes). Such a mechanism then opens the doors for bitcoin (or a layer on top of bitcoin like lightning) to be able
 to accept and pay for *any* proof of work, not simply sha256.

 Lastly, below we do a quick calculation of satoshis rewarded per bit of work:
**/
def heightWithRewardWithWork = (workSequence zipWithIndex) map {case (w,i) => ((i+1)*2016,w)} map {case (b,w) => (b,BigDecimal(50.0) / BigDecimal(2).pow((b / 210000)),w)}

def heightWithSatoshisPerBitOfWork = heightWithRewardWithWork.map{case (b,r,w) => (b,r*BigDecimal(100000000)*BigDecimal(2016) / w)}
/**
    (height, satoshis_per_bit_of_work)
    (2016,149509481.6345340189859260376356576)
    (4032,149509481.6345340189859260376356576)
    (6048,149509481.6345340189859260376356576)
    (8064,149509481.6345340189859260376356576)
    (10080,149509481.6345340189859260376356576)
    (12096,149509481.6345340189859260376356576)
    ... elided ...
    (592704,16289818.87376214346830753837298463)
    (594720,16259616.66384108717423908746869959)
    (596736,16238117.16670792075079185206091653)
    (598752,16232254.82036631098871295722314663)
    (600768,16216698.96184540730480516339619917)
    (602784,16239068.28059745494754200528817087)


 The above is simply looking at the rewards purely as a function of block subsidy and not accounting for fees. However, this is a 
 first step towards understanding how an idealized "fee market" which should 
 probably more aptly be called a "work market" may be encouraged to develop.
 
 Further research is certainly necessary.
**/


/*
Adding some more quick thoughts here: 
If we take the block in the example (#602784) which has 76.97 bits of work, and we look at the
size of the block (also measured in bits), we can begin reframing our discussion thus. Miners buy bits of space in exchange for bits
of work. For this block, they paid 76.97 bits, and received 9999944 bits of space. That works out to be approximately 16.2 kiloBytes
of space per bit of work. Some of this space is unusable since it was used to communicate the proof-of-work itself, but otherwise
it is that simple. 

In other words, it is not the nodes (e.g. transactions) which are buying block space from the miners, it is
actually the miners which are buying blockspace from the nodes. Of course, the nodes are smart, and resource concious, so they only
form of payment which they (collectively) will accept is bits of work. This makes sense because bits of work represent, almost by definition,
energy which is unavailable for future work by the system (2nd law of thermodynamics). As such, it is a form of payment, and perhaps the only
form of payment, which can be universally verified.

Notice that there is no mention of "satoshis" in the prior arguments. Satoshis are usually considered to be the reward which miners receive. Yet,
by the above arguments, it seems that when we look at this through a purely information-theoretic lense, the actual trade that is happening is
bits of past work in exchange for representation of bits in future space. Now, we are really reaching here, but can this be taken a step further
to infer that proof-of-work *is* the singularity? Ha, probably a bit to far.
*/



// misc notes: 
// for reference, an old stack exchange answer: https://bitcoin.stackexchange.com/questions/26869/what-is-chainwork
// however, the answer does not translate work all the way down into an independent representation of bits
// instead they stop short and talk only about number of hashes




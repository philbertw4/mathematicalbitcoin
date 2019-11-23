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
val current_difficulty = BigInt("12720005267390")

// approximating max target as 2^224 per https://bitcoin.stackexchange.com/questions/8806/what-is-difficulty-and-how-it-relates-to-target
// why this is not 2^256 is because the lowest difficulty (a difficulty value of 1) was based on iterating through 2^32 hashes
val max_target = BigInt(2).pow(224)

val current_target = max_target / current_difficulty

// The number of hashes, in expecation, it takes to find a block is given by 2^32 * current_difficulty.
val num_hashes = current_difficulty * BigInt(2).pow(32)

/**
  While num_hashes is a good start towards an estimate of time, but what we are really after is something more general.

  If we think about this in a more information-theoretic way, there is a probability p = current_target / max_target
  that a flip of this "hash coin" will result in a number h such that h is less than or equal to current_target. This
  is predicated on the assumption that the output of the hash function approximates a uniform distribution.

  Calculating p is straightforward:
*/
val p = BigDecimal(current_target) / BigDecimal(max_target)

// Now, let w be the entropy (in bits) pertaining to the number of failures (hashes) until the first success
// This is a geometric distribution (https://en.wikipedia.org/wiki/Geometric_distribution)
val w = (-(BigDecimal(1) - p)*log(BigDecimal(1)-p,2) - p*log(p,2)) / p
// Performing the calculation, we get w = 44.974859 bits of work.

/* Clearly w is a function of p and p is a function of difficulty. So, at least for bitcoin, we can create a simple function
   which performs the above steps for any difficulty it is given. Note, of course, that this function simply calculates what
   the expected minimum work must be in order to achieve or exceed the desired difficulty.
*/

def calcMinimumBitcoinWork(difficulty: BigInt): BigDecimal = {
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

    // now we return w
    w
}

/**
 The above function calculates the minimum work because, naturally, there are other protocol-specific constraints which go into building 
 a block (for example, a block must have a valid timestamp, transactions must be valid, etc). While it may be possible to also account for
 these additional constraints in a similarly information-theoretic way, the proof of work constraint, due to its massive number of iterations, 
 might dominate. For now we will assume that to be the case. Nevertheless, this may not be a wise assumption.

 Now back to our value of w. With w = 44.97 bits/block (at the example block height of 602,784), we are basically saying that, in expecation, 
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
def accumWorkSequence = workSequence.scanLeft(BigDecimal(0.0)){case (tot:BigDecimal, diff:BigDecimal) => tot + diff}

//Now, stopping for a moment to check our work, if we take look at the last value of the accumWorkSequence
def accumulatedWork = accumWorkSequence.last
/** 
 This gives us 17589203.10334326 bits of accumulated work. This is approximately 17.5 million bits or approximately 2.2 megabytes.

 Can we safely interpret this to mean that it is fundamentally impossible to faithfully encode the bitcoin blockchain in fewer 
 than 2.2 megabytes? It seems so.
*/


/* Moving on, it may be helpful to perform these calculations and compare the results to how bitcoin currently measures its 
accumulated work (e.g accumulated number of hashes rather than bits of work). Here we calculate the base 2 logarithm of the accumulated 
number of hashes.
*/
def numHashesSequence = readdifficultySequenceFromFile.map(_ * BigInt(2).pow(32) * BigInt(2016))
def accumNumHashesSequenceLog2 = (numHashesSequence.scanLeft(BigInt(1)){case (tot: BigInt, hashes: BigInt) => tot + hashes}).map(n => BigDecimal(n).log(2))

def writeCsv = write(pwd/"accumWork.csv",(readdifficultySequenceFromFile zip accumNumHashesSequenceLog2 zip accumWorkSequence zipWithIndex).map{case(((d,h),w),i) => s"${(i+1)*2016},$d,$h,$w\n"})
/** this output is also available in the git repository as accumWork.csv
    height,difficulty,accum_hashes,accum_work
    2016,1,0E+37,0.0
    4032,1,42.977279923500083089206077365700758589,0.0
    6048,1,43.977279923499999779777762287819491647,0.0
    8064,1,44.562242424221128191422062871404494537,0.0
    10080,1,44.977279923499958125063604747074822850,0.0
    12096,1,45.299208018387312141991092668270956441,0.0
    14112,1,45.562242424221100421612624510507151935,0.0
    16128,1,45.784634845557544380485220832190767986,0.0
    18144,1,45.977279923499937297706525976251479621,0.0
    20160,1,46.147204924942247346463217334037070023,0.0
    22176,1,46.299208018387295480105429651540120445,0.0
    24192,1,46.436711542137228873717594812700541586,0.0
    26208,1,46.562242424221086536707905329858032264,0.0
    28224,1,46.677719641641021447581385257034520066,0.0
    30240,1,46.784634845557532479138318677324120714,0.0
    32256,1,46.884170519108446107597280923201562637,0.0
    34272,1,46.977279923499926884027986590727055799,0.0
    36288,1,47.064742764750265679712726755327800228,0.0
    38304,1,47.147204924942238089860071213559779495,0.0
    40320,2,47.225207436943510733556473479070530925,0.0
    42336,3,47.369597346278684693476423569627903955,4031.999999999999999999999999999998
    44352,4,47.562242424221079594255545739483360337,9585.853204361552585432213132996391
    46368,4,47.784634845557526528464867599853980030,16128.00000000000000000000000000000
    48384,6,47.977279923499921677188716897936655837,22670.14679563844741456778686700360
    50400,7,48.225207436943506348849720053559077697,30532.81800789694011889939328374334
    52416,11,48.469133019829594883768502970734287140,38882.50425925074411225603802252615
    54432,12,48.784634845557523553128142061109705426,48628.78944122222313266747324821124
    56448,11,49.064742764750258328880816600791593538,58639.84668376772952091925085600563
    58464,16,49.281060671677021503876184561879668264,68386.13186573920854133068608169072
    60480,17,49.547135531830866065895987427735199357,79265.76025453760821457116086894959
    ....elided...
    570528,6393023717201,90.5177866993516850212747624961205736075,16063796.95547973359576827325060541
    572544,6353030562983,90.5621544267059311862260576389519739844,16152465.33402090401748631148676849
    574560,6702169884349,90.6049329897981667713439679552954743909,16241115.46073347039665104662651218
    576576,6704632680587,90.6487284630669222949030508157517985946,16329921.18897142213903096539485378
    578592,7459680720542,90.6912489174277881053324205952443748319,16418727.98576795183484936713725225
    580608,7409399249090,90.7371301195992595826114824390014456191,16507845.15703096096686050374479017
    582624,7934713219630,90.7813018654782636025090639291914974040,16596942.65755845164820593053219310
    584640,9064159826491,90.8271521280184717002339143995652471236,16686239.38224449637399367930233372
    586656,9013786945891,90.8778055585665470161893463120588653163,16775923.16929150879220032952726836
    588672,9985348008059,90.9264732773219864879254577601757991801,16865590.74779953471924989168580543
    590688,10183488432890,90.9785366611035809193225468656033477775,16955556.04789516180380420956432538
    592704,10771996663680,91.0297660757473525573553862288282640579,17045578.49602293686260660055749587
    594720,11890594958795,91.0820464050887502112008691591733183613,17135764.34868342372769090298306573
    596736,12759819404408,91.1376376368821511022585479594365420805,17226237.55233851943248331192508581
    598752,13008091666971,91.1950022344839190516935111145453688531,17316915.95859856320193168669392511
    600768,13691480038694,91.2512256772816108744332475045157418729,17407650.41251930268515642118364336
    602784,12720005267390,91.3081275449810860860464939716200853658,17498533.78650564005554251053858774
*/


/**

 At first glance, it may seem strange or wrong that our calculations do not attribute any work in the information-theoretic absolute sence to the
 periods where difficulty is 1. There may in fact be an error in our calculations (though the methodology itself hopefully will still withstand
 scrutiny). However, it may also make a sense due to the fact that with difficuly 1, nearly any hash will do to solve a block. Though, the more likely
 scenario is that we have an off-by-one error somewhere in our code.


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
def heightWithRewardWithWork = (workSequence zipWithIndex) map {case (w,i) => (i*2016,w)} map {case (b,w) => (b,BigDecimal(50.0) / BigDecimal(2).pow((b / 210000)),w)}

/**
  
  val heightWithSatoshisPerByteOfWork = heightWithRewardWithWork.filter(_._3 > BigDecimal(0.0)).map{case (b,r,w) => (b,r*BigDecimal(100000000) / w)} 
  (height, satoshis_per_bit_of_work)
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

 The above is simply looking at the rewards purely as a function of block subsidy and not accounting for fees. Additionally, it ignores the
 first periods where difficulty was 1. However, this is a first step towards understanding how an idealized "fee market" which should 
 probably more aptly be called a "work market" may be encouraged to develop.
 
 Further research is certainly necessary.
**/






// misc notes: 
// for reference, an old stack exchange answer: https://bitcoin.stackexchange.com/questions/26869/what-is-chainwork
// however, the answer does not translate work all the way down into an independent representation of bits
// instead they stop short and talk only about number of hashes




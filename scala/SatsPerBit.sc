/**
Here we will attempt to do a simple derivation around the conversion from energy to satoshis and back
**/

// first some imports
import $ivy.`org.typelevel::spire:0.17.0-M1` 
import spire.math._
import spire.implicits._

/**
  Bitcoin's current target is a 256 bit number and can be calculated from the current difficulty.
  Difficulty = Max_Target / Current_target which implies that Current_target = Max_Target / Difficulty
*/

val difficulty = BigInt("12720005267390")

// approximating max target as 2^224 per https://bitcoin.stackexchange.com/questions/8806/what-is-difficulty-and-how-it-relates-to-target
val max_target = BigInt(2).pow(224)

val current_target = max_target / difficulty

// it takes, on average, 2^32 * difficulty hashes to find a block

val num_hashes = difficulty * BigInt(2).pow(32)

// let p be the probability of flipping heads on this extremely biased hash coin

val p = BigDecimal(current_target) / BigDecimal(max_target)

// let h be the entropy (in bits) pertaining to the number of failures (hashes) until the first success
// this is a geometric distribution

val h = (-(BigDecimal(1) - p)*log(BigDecimal(1)-p,2) - p*log(p,2)) / p

// currently h = 44.974859 bits





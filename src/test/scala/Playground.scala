import org.bitdomains.contracts._
import org.ergoplatform.appkit.{Address, ConstantsBuilder}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import scorex.crypto.hash.Blake2b256

// Just a place to test random ErgoScript
class Playground
    extends AnyFlatSpec
    with should.Matchers
    with WithBlockchainContext {
  it should "execute playground test!" in {
    withBlockchain { implicit ctx =>
      val script =
        s"""{
           |  val c1 = Coll(Coll(4, 2), Coll(1), Coll(5))
           |  val c3 = c1.flatMap{(v: Coll[Int]) => v}
           |
           |  val expected = Coll(4, 2, 1, 5)
           |  val sameEl = c3 == expected
           |
           |  val expectedLastEl = 5
           |  val lastEl = c3(c3.size - 1)
           |  val sameLastEl = lastEl == expectedLastEl
           |
           |  val s1 = fromBase16("74657374") // "test"
           |  val s2 = fromBase16("737562") // "sub"
           |  val s3 = fromBase16("657267") // "erg"
           |  val strs = Coll(s1, s2, s3)
           |
           |  //val combined = strs.fold(Coll[Coll[Byte]](), {(a: Coll[Byte], b: Coll[Byte]) => a ++ b})
           |
           |  val combined = strs.flatMap{(v: Coll[Byte]) => v}
           |  val actualHash = blake2b256(combined)
           |  val expectedHash = blake2b256(s1 ++ s2 ++ s3)
           |
           |  sigmaProp(expectedHash == actualHash)
           |}""".stripMargin
      val tb = ctx.newTxBuilder()
      val minStorageRent = 100000L

      val map = defaultRegistryMap
      map.ergoAVLTree
      val tld = "test"
//      map.insert((Blake2b256(tld), bytesToHex(tld.getBytes)))
      println(map.digest.length)
      println(bytesToHex(map.digest))

      val prover = ctx
        .newProverBuilder()
        .withDLogSecret(BigInt.apply(0).bigInteger)
        .build()

      val inBox =
        tb.outBoxBuilder
          .value(minStorageRent + 10000000000000L)
          .contract(ctx.compileContract(ConstantsBuilder.empty(), script))
          .build()
          .convertToInputWith(fakeTxId3, fakeIndex)

      val outBox =
        tb.outBoxBuilder
          .value(minStorageRent)
          .contract(ctx.compileContract(ConstantsBuilder.empty(), script))
          .build()

      val tx = tb
        .fee(1e7.toLong)
        .addInputs(inBox)
        .addOutputs(outBox)
        .sendChangeTo(Address.create("4MQyML64GnzMxZgm"))
        .build()

      val _ = prover.sign(tx)
    }
  }
}

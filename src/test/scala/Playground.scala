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
           |  val a = 5
           |  val b = 6
           |
           |  val expected = -11
           |
           |  val t = expected == - b - a
           |
           |  sigmaProp(t)
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

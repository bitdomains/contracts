package org.bitdomains.contracts

import bitdomains.Constants.resolverScript
import bitdomains.Utils
import io.getblok.getblok_plasma.PlasmaParameters
import io.getblok.getblok_plasma.collections.PlasmaMap
import org.ergoplatform.ErgoAddressEncoder
import org.ergoplatform.ErgoAddressEncoder.TestnetNetworkPrefix
import org.ergoplatform.appkit._
import org.ergoplatform.wallet.secrets.ExtendedSecretKey
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import scorex.crypto.hash.Blake2b256
import sigmastate.AvlTreeFlags
import sigmastate.eval.CostingSigmaDslBuilder.GroupElement
import sigmastate.lang.exceptions.InterpreterException

class ResolverSpec extends AnyFlatSpec with should.Matchers {
//  val ergoClient: ErgoClient = RestApiErgoClient.create(
//    "http://127.0.0.1:9052/",
//    NetworkType.TESTNET,
//    "",
//    ""
//  )
  val addrEnc = new ErgoAddressEncoder(NetworkType.TESTNET.networkPrefix)
  val fakeIndex: Short = 1
  val fakeTxId1 =
    "f9e5ce5aa0d95f5d54a7bc89c46730d9662397067250aa18a0039631c0f5b809"
  val fakeTxId2 =
    "f9e5ce5aa0d95f5d54a7bc89c46730d9662397067250aa18a0039631c0f5b808"
  val fakeTxId3 =
    "f9e5ce5aa0d95f5d54a7bc89c46730d9662397067250aa18a0039631c0f5b807"
  val fakeTxId4 =
    "f9e5ce5aa0d95f5d54a7bc89c46730d9662397067250aa18a0039631c0f5b806"
  val fakeTxId5 =
    "f9e5ce5aa0d95f5d54a7bc89c46730d9662397067250aa18a0039631c0f5b105"
  val changeAddress = "9gQqZyxyjAptMbfW1Gydm3qaap11zd6X9DrABwgEE9eRdRvd27p"
  val fakeScript = "sigmaProp(true)"
  lazy val minStorageRent = 100000L
  val mnemonic = SecretString.create("not real mnemonic")
  val rootSecret =
    JavaHelpers.seedToMasterKey(mnemonic, SecretString.empty(), true)
  val path = JavaHelpers.eip3DerivationParent
  val secretKey = rootSecret.derive(path).asInstanceOf[ExtendedSecretKey]
  val pkGe = GroupElement(secretKey.publicImage.value)
  val resolverNft =
    "94af8793a1f7b427831dcb48368ffc55c68d319d525ea24510ac38b75e280a8c"

  "Test" should "work on plasma" in {
    val script =
      s"""{
         |  val selfTree = SELF.R4[AvlTree].get
         |  val treeKey = SELF.R5[Coll[Byte]].get
         |  val containsProof = getVar[Coll[Byte]](0).get
         |  val contains = selfTree.contains(treeKey, containsProof)
         |  
         |  sigmaProp(selfTree.digest == OUTPUTS(0).R4[AvlTree].get.digest && contains)
         |}""".stripMargin

    ergoClient.execute(ctx => {
      val registrarsMap = new PlasmaMap[Array[Byte], String](
        AvlTreeFlags.AllOperationsAllowed,
        PlasmaParameters.default
      )

      val prover = ctx
        .newProverBuilder()
        .withDLogSecret(BigInt.apply(0).bigInteger)
        .withDLogSecret(secretKey.privateInput.w)
        .build()

      val mapKey = Blake2b256("hello")

      val insertOp = registrarsMap.insert((mapKey, "01"))
      val op = registrarsMap.lookUp((mapKey))

      val tb = ctx.newTxBuilder()

      var inBox =
        tb.outBoxBuilder
          .value(minStorageRent + 10000000000000L)
          .registers(registrarsMap.ergoValue, ErgoValue.of(mapKey))
          .contract(ctx.compileContract(ConstantsBuilder.empty(), script))
          .build()
          .convertToInputWith(fakeTxId2, fakeIndex)
          .withContextVars(new ContextVar(0.toByte, op.proof.ergoValue))

      val outBox =
        tb.outBoxBuilder
          .value(minStorageRent)
          .registers(registrarsMap.ergoValue)
          .contract(ctx.compileContract(ConstantsBuilder.empty(), script))
          .build()

      val tx = tb
        .fee(1e7.toLong)
        .addInputs(inBox)
        .addOutputs(outBox)
        .sendChangeTo(Address.create("4MQyML64GnzMxZgm"))
        .build()

      val _ = prover.sign(tx)
    })
  }

  "ResolverSpec" should "not be possible to change nft" in {
    ergoClient.execute(ctx => {
      val prover = ctx
        .newProverBuilder()
        .withDLogSecret(secretKey.privateInput.w)
        .build()

      val tld = "erg"
      val label = "helloworld"
      val resolvedToAddress = "4MQyML64GnzMxZgm"

      val tb = ctx.newTxBuilder()

      val resolverInBox =
        tb.outBoxBuilder
          .value(300000000000000000L)
          .tokens(new ErgoToken(resolverNft, 1))
          .registers(
            ErgoValue.of(pkGe),
            ErgoValue.of(label.getBytes),
            ErgoValue.of(tld.getBytes),
            ErgoValue.of(resolvedToAddress.getBytes)
          )
          .contract(
            ctx.compileContract(ConstantsBuilder.empty(), resolverScript)
          )
          .build()
          .convertToInputWith(fakeTxId3, fakeIndex)

      val resolverOutBox =
        tb
          .outBoxBuilder()
          .value(200000000000000000L)
          .tokens(new ErgoToken(resolverInBox.getId, 1))
          .registers(
            ErgoValue.of(pkGe),
            ErgoValue.of(label.getBytes),
            ErgoValue.of(tld.getBytes),
            ErgoValue.of(resolvedToAddress.getBytes)
          )
          .contract(
            ctx.compileContract(ConstantsBuilder.empty(), resolverScript)
          )
          .build()

      val stolenNftBox =
        tb
          .outBoxBuilder()
          .value(50000000000000L)
          .tokens(new ErgoToken(resolverNft, 1))
          .contract(ctx.compileContract(ConstantsBuilder.empty(), fakeScript))
          .build()

      val tx = tb
        .fee(1e7.toLong)
        .addInputs(resolverInBox)
        .addOutputs(resolverOutBox, stolenNftBox)
        .sendChangeTo(Address.create("4MQyML64GnzMxZgm"))
        .build()

      (the[InterpreterException] thrownBy prover.sign(tx)).getMessage should be(
        "Script reduced to false"
      )
    })
  }

  "ResolverSpec" should "not be possible to change script" in {
    ergoClient.execute(ctx => {
      val prover = ctx
        .newProverBuilder()
        .withDLogSecret(secretKey.privateInput.w)
        .build()

      val tld = "erg"
      val label = "helloworld"
      val resolvedToAddress = "4MQyML64GnzMxZgm"

      val tb = ctx.newTxBuilder()

      val resolverInBox =
        tb.outBoxBuilder
          .value(300000000000000000L)
          .tokens(new ErgoToken(resolverNft, 1))
          .registers(
            ErgoValue.of(pkGe),
            ErgoValue.of(label.getBytes),
            ErgoValue.of(tld.getBytes),
            ErgoValue.of(resolvedToAddress.getBytes)
          )
          .contract(
            ctx.compileContract(ConstantsBuilder.empty(), resolverScript)
          )
          .build()
          .convertToInputWith(fakeTxId3, fakeIndex)

      val resolverOutBox =
        tb
          .outBoxBuilder()
          .value(200000000000000000L)
          .tokens(new ErgoToken(resolverNft, 1))
          .registers(
            ErgoValue.of(pkGe),
            ErgoValue.of(label.getBytes),
            ErgoValue.of(tld.getBytes),
            ErgoValue.of(resolvedToAddress.getBytes)
          )
          .contract(ctx.compileContract(ConstantsBuilder.empty(), fakeScript))
          .build()

      val tx = tb
        .fee(1e7.toLong)
        .addInputs(resolverInBox)
        .addOutputs(resolverOutBox)
        .sendChangeTo(Address.create("4MQyML64GnzMxZgm"))
        .build()

      (the[InterpreterException] thrownBy prover.sign(tx)).getMessage should be(
        "Script reduced to false"
      )
    })
  }

  "ResolverSpec" should "not be possible to change tld" in {
    ergoClient.execute(ctx => {
      val prover = ctx
        .newProverBuilder()
        .withDLogSecret(secretKey.privateInput.w)
        .build()

      val tld = "erg"
      val label = "helloworld"
      val resolvedToAddress = "4MQyML64GnzMxZgm"

      val tb = ctx.newTxBuilder()

      val resolverInBox =
        tb.outBoxBuilder
          .value(300000000000000000L)
          .tokens(new ErgoToken(resolverNft, 1))
          .registers(
            ErgoValue.of(pkGe),
            ErgoValue.of(label.getBytes),
            ErgoValue.of(tld.getBytes),
            ErgoValue.of(resolvedToAddress.getBytes)
          )
          .contract(
            ctx.compileContract(ConstantsBuilder.empty(), resolverScript)
          )
          .build()
          .convertToInputWith(fakeTxId3, fakeIndex)

      val resolverOutBox =
        tb
          .outBoxBuilder()
          .value(200000000000000000L)
          .tokens(new ErgoToken(resolverNft, 1))
          .registers(
            ErgoValue.of(pkGe),
            ErgoValue.of(label.getBytes),
            ErgoValue.of("ada".getBytes),
            ErgoValue.of(resolvedToAddress.getBytes)
          )
          .contract(
            ctx.compileContract(ConstantsBuilder.empty(), resolverScript)
          )
          .build()

      val tx = tb
        .fee(1e7.toLong)
        .addInputs(resolverInBox)
        .addOutputs(resolverOutBox)
        .sendChangeTo(Address.create("4MQyML64GnzMxZgm"))
        .build()

      (the[InterpreterException] thrownBy prover.sign(tx)).getMessage should be(
        "Script reduced to false"
      )
    })
  }

  "ResolverSpec" should "not be possible to change label" in {
    ergoClient.execute(ctx => {
      val prover = ctx
        .newProverBuilder()
        .withDLogSecret(secretKey.privateInput.w)
        .build()

      val tld = "erg"
      val label = "helloworld"
      val resolvedToAddress = "4MQyML64GnzMxZgm"

      val tb = ctx.newTxBuilder()

      val resolverInBox =
        tb.outBoxBuilder
          .value(300000000000000000L)
          .tokens(new ErgoToken(resolverNft, 1))
          .registers(
            ErgoValue.of(pkGe),
            ErgoValue.of(label.getBytes),
            ErgoValue.of(tld.getBytes),
            ErgoValue.of(resolvedToAddress.getBytes)
          )
          .contract(
            ctx.compileContract(ConstantsBuilder.empty(), resolverScript)
          )
          .build()
          .convertToInputWith(fakeTxId3, fakeIndex)

      val resolverOutBox =
        tb
          .outBoxBuilder()
          .value(200000000000000000L)
          .tokens(new ErgoToken(resolverNft, 1))
          .registers(
            ErgoValue.of(pkGe),
            ErgoValue.of("newlabel".getBytes),
            ErgoValue.of(tld.getBytes),
            ErgoValue.of(resolvedToAddress.getBytes)
          )
          .contract(
            ctx.compileContract(ConstantsBuilder.empty(), resolverScript)
          )
          .build()

      val tx = tb
        .fee(1e7.toLong)
        .addInputs(resolverInBox)
        .addOutputs(resolverOutBox)
        .sendChangeTo(Address.create("4MQyML64GnzMxZgm"))
        .build()

      (the[InterpreterException] thrownBy prover.sign(tx)).getMessage should be(
        "Script reduced to false"
      )
    })
  }

  "ResolverSpec" should "should not be spendable by non-owners" in {
    ergoClient.execute(ctx => {
      val prover = ctx
        .newProverBuilder()
        .withDLogSecret(BigInt.apply(0).bigInteger)
        .build()

      val tld = "erg"
      val label = "helloworld"
      val resolvedToAddress = "4MQyML64GnzMxZgm"

      val tb = ctx.newTxBuilder()

      val resolverInBox =
        tb.outBoxBuilder
          .value(300000000000000000L)
          .tokens(new ErgoToken(resolverNft, 1))
          .registers(
            ErgoValue.of(pkGe),
            ErgoValue.of(label.getBytes),
            ErgoValue.of(tld.getBytes),
            ErgoValue.of(resolvedToAddress.getBytes)
          )
          .contract(
            ctx.compileContract(ConstantsBuilder.empty(), resolverScript)
          )
          .build()
          .convertToInputWith(fakeTxId3, fakeIndex)

      val resolverOutBox =
        tb
          .outBoxBuilder()
          .value(200000000000000000L)
          .tokens(new ErgoToken(resolverNft, 1))
          .registers(
            ErgoValue.of(pkGe),
            ErgoValue.of(label.getBytes),
            ErgoValue.of(tld.getBytes),
            ErgoValue.of(resolvedToAddress.getBytes)
          )
          .contract(
            ctx.compileContract(ConstantsBuilder.empty(), resolverScript)
          )
          .build()

      val tx = tb
        .fee(1e7.toLong)
        .addInputs(resolverInBox)
        .addOutputs(resolverOutBox)
        .sendChangeTo(Address.create("4MQyML64GnzMxZgm"))
        .build()

      (the[AssertionError] thrownBy prover.sign(tx)).getMessage should include(
        "Tree root should be real but was UnprovenSchnorr"
      )
    })
  }

  "ResolverSpec" should "be transferable to new owner" in {
    ergoClient.execute(ctx => {
      val prover = ctx
        .newProverBuilder()
        .withDLogSecret(secretKey.privateInput.w)
        .build()

      val tld = "erg"
      val label = "helloworld"
      val resolvedToAddress = "4MQyML64GnzMxZgm"
      val newOwnerAddr =
        Address.create("3WwhifgHTu7ib5ggKKVFaN1J6jFim3u9siPspDRq9JnwcKfLuuxc")

      val tb = ctx.newTxBuilder()

      val resolverInBox =
        tb.outBoxBuilder
          .value(300000000000000000L)
          .tokens(new ErgoToken(resolverNft, 1))
          .registers(
            ErgoValue.of(pkGe),
            ErgoValue.of(label.getBytes),
            ErgoValue.of(tld.getBytes),
            ErgoValue.of(resolvedToAddress.getBytes)
          )
          .contract(
            ctx.compileContract(ConstantsBuilder.empty(), resolverScript)
          )
          .build()
          .convertToInputWith(fakeTxId3, fakeIndex)

      val resolverOutBox =
        tb
          .outBoxBuilder()
          .value(200000000000000000L)
          .tokens(new ErgoToken(resolverNft, 1))
          .registers(
            ErgoValue.of(newOwnerAddr.getPublicKeyGE),
            ErgoValue.of(label.getBytes),
            ErgoValue.of(tld.getBytes),
            ErgoValue.of(resolvedToAddress.getBytes)
          )
          .contract(
            ctx.compileContract(ConstantsBuilder.empty(), resolverScript)
          )
          .build()

      val tx = tb
        .fee(1e7.toLong)
        .addInputs(resolverInBox)
        .addOutputs(resolverOutBox)
        .sendChangeTo(Address.create("4MQyML64GnzMxZgm"))
        .build()

      noException should be thrownBy prover.sign(tx)
    })
  }

  "ResolverSpec" should "be possible to update resolved-to address" in {
    ergoClient.execute(ctx => {
      val prover = ctx
        .newProverBuilder()
        .withDLogSecret(secretKey.privateInput.w)
        .build()

      val tld = "erg"
      val label = "helloworld"
      val resolvedToAddress = "4MQyML64GnzMxZgm"

      val tb = ctx.newTxBuilder()

      val resolverInBox =
        tb.outBoxBuilder
          .value(300000000000000000L)
          .tokens(new ErgoToken(resolverNft, 1))
          .registers(
            ErgoValue.of(pkGe),
            ErgoValue.of(label.getBytes),
            ErgoValue.of(tld.getBytes),
            ErgoValue.of(resolvedToAddress.getBytes)
          )
          .contract(
            ctx.compileContract(ConstantsBuilder.empty(), resolverScript)
          )
          .build()
          .convertToInputWith(fakeTxId3, fakeIndex)

      val resolverOutBox =
        tb
          .outBoxBuilder()
          .value(200000000000000000L)
          .tokens(new ErgoToken(resolverNft, 1))
          .registers(
            ErgoValue.of(pkGe),
            ErgoValue.of(label.getBytes),
            ErgoValue.of(tld.getBytes),
            ErgoValue.of("MMQyML64GnzMxZgm".getBytes)
          )
          .contract(
            ctx.compileContract(ConstantsBuilder.empty(), resolverScript)
          )
          .build()

      val tx = tb
        .fee(1e7.toLong)
        .addInputs(resolverInBox)
        .addOutputs(resolverOutBox)
        .sendChangeTo(Address.create("4MQyML64GnzMxZgm"))
        .build()

      noException should be thrownBy prover.sign(tx)
    })
  }
}

package org.bitdomains.contracts

import org.bitdomains.contracts.boxes.{
  NewRegistrarBoxBuilder,
  RegistryAdminBoxBuilder,
  RegistryBoxBuilder
}
import org.bitdomains.contracts.transactions.NewRegistrarTransaction
import org.ergoplatform.appkit._
import org.ergoplatform.ErgoAddressEncoder
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import scorex.crypto.hash.Blake2b256
import sigmastate.lang.exceptions.InterpreterException

class NewRegistrarSpec extends AnyFlatSpec with should.Matchers {
  val ergoClient: ErgoClient = RestApiErgoClient.create(
    "http://168.138.185.215:9052/",
    NetworkType.TESTNET,
    "",
    ""
  )
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

  def bytesToHex(bytes: Array[Byte]): String = {
    bytes.map("%02x".format(_)).mkString
  }

  "NewRegistrar" should "fail if RegistryAdmin data input is missing" in {
    ergoClient.execute(implicit ctx => {
      val prover = ctx
        .newProverBuilder()
        .withDLogSecret(BigInt.apply(0).bigInteger)
        .build()

      val registrarsMap = defaultRegistryMap
      val tldToMint = "erg"

      val registryInBox =
        RegistryBoxBuilder()
          .withRegistrarsMap(registrarsMap)
          .withValue(200000000000000000L)
          .build()
          .convertToInputWith(fakeTxId3, fakeIndex)
      var newRegistrarInBox = NewRegistrarBoxBuilder()
        .withTld(tldToMint)
        .build()
        .convertToInputWith(fakeTxId3, fakeIndex)

      val hashedTld = Blake2b256.hash(tldToMint)
      val updatedRegistrarsMapResult = registrarsMap.insert((hashedTld, "01"))

      newRegistrarInBox = newRegistrarInBox.withContextVars(
        new ContextVar(0.toByte, updatedRegistrarsMapResult.proof.ergoValue)
      )

      val registryOutBox =
        RegistryBoxBuilder().withRegistrarsMap(registrarsMap).build()
      val newRegistrarOutBox = NewRegistrarBoxBuilder().build()

      val tx = NewRegistrarTransaction()
        .partialBuild()
        .addInputs(Seq(registryInBox, newRegistrarInBox): _*)
        .addOutputs(Seq(registryOutBox, newRegistrarOutBox): _*)
        .build()

      an[ArrayIndexOutOfBoundsException] should be thrownBy prover.sign(tx)
    })
  }

  "NewRegistrar" should "fail if RegistryAdmin data input has incorrect nft" in {
    ergoClient.execute(implicit ctx => {
      val prover = ctx
        .newProverBuilder()
        .withDLogSecret(BigInt.apply(0).bigInteger)
        .build()

      val registrarsMap = defaultRegistryMap
      val tldToMint = "erg"

      val registryAdmin = RegistryAdminBoxBuilder()
        .withNftId(
          "94af8793a1f7b427831dcb48368ffc55c68d319d525ea24510ac38b75e280a8e"
        )
        .build()
      val registryInBox =
        RegistryBoxBuilder()
          .withRegistrarsMap(registrarsMap)
          .withValue(200000000000000000L)
          .build()
          .convertToInputWith(fakeTxId3, fakeIndex)
      var newRegistrarInBox = NewRegistrarBoxBuilder()
        .withTld(tldToMint)
        .build()
        .convertToInputWith(fakeTxId3, fakeIndex)

      val hashedTld = Blake2b256.hash(tldToMint)
      val updatedRegistrarsMapResult = registrarsMap.insert((hashedTld, "01"))

      newRegistrarInBox = newRegistrarInBox.withContextVars(
        new ContextVar(0.toByte, updatedRegistrarsMapResult.proof.ergoValue)
      )

      val registryOutBox =
        RegistryBoxBuilder().withRegistrarsMap(registrarsMap).build()
      val newRegistrarOutBox = NewRegistrarBoxBuilder().build()

      val tx = NewRegistrarTransaction()
        .withRegistryIn(registryInBox)
        .withNewRegistrarIn(newRegistrarInBox)
        .withRegistryOut(registryOutBox)
        .withNewRegistrarOut(newRegistrarOutBox)
        .withAdminIn(registryAdmin.convertToInputWith(fakeTxId1, fakeIndex))
        .withAdminOut(registryAdmin)
        .build()

      (the[InterpreterException] thrownBy prover.sign(tx)).getMessage should be(
        "Script reduced to false"
      )
    })
  }

  "NewRegistrar" should "fail if registrars state in Registry isn't updated correctly" in {
    ergoClient.execute(implicit ctx => {
      val prover = ctx
        .newProverBuilder()
        .withDLogSecret(BigInt.apply(0).bigInteger)
        .build()

      val registrarsMap = defaultRegistryMap

      val registryInBox =
        RegistryBoxBuilder()
          .withRegistrarsMap(registrarsMap)
          .withValue(200000000000000000L)
          .build()
          .convertToInputWith(fakeTxId3, fakeIndex)
      var newRegistrarInBox = NewRegistrarBoxBuilder()
        .withTld("erg")
        .build()
        .convertToInputWith(fakeTxId3, fakeIndex)

      val hashedTld = Blake2b256.hash("noterg") // different tld to register
      val updatedRegistrarsMapResult = registrarsMap.insert((hashedTld, "01"))

      newRegistrarInBox = newRegistrarInBox.withContextVars(
        new ContextVar(0.toByte, updatedRegistrarsMapResult.proof.ergoValue)
      )

      val registryOutBox =
        RegistryBoxBuilder().withRegistrarsMap(registrarsMap).build()
      val newRegistrarOutBox = NewRegistrarBoxBuilder().build()

      val tx = NewRegistrarTransaction()
        .withRegistryIn(registryInBox)
        .withNewRegistrarIn(newRegistrarInBox)
        .withRegistryOut(registryOutBox)
        .withNewRegistrarOut(newRegistrarOutBox)
        .build()

      (the[InterpreterException] thrownBy prover.sign(tx)).getMessage should be(
        "Script reduced to false"
      )
    })
  }

  "NewRegistrar" should "fail if successor script changed" in {
    ergoClient.execute(implicit ctx => {
      val prover = ctx
        .newProverBuilder()
        .withDLogSecret(BigInt.apply(0).bigInteger)
        .build()

      val registrarsMap = defaultRegistryMap
      val tldToMint = "erg"

      val registryInBox =
        RegistryBoxBuilder()
          .withRegistrarsMap(registrarsMap)
          .withValue(200000000000000000L)
          .build()
          .convertToInputWith(fakeTxId3, fakeIndex)
      var newRegistrarInBox = NewRegistrarBoxBuilder()
        .build()
        .convertToInputWith(fakeTxId3, fakeIndex)

      val hashedTld = Blake2b256.hash(tldToMint)
      val updatedRegistrarsMapResult = registrarsMap.insert((hashedTld, "01"))

      newRegistrarInBox = newRegistrarInBox.withContextVars(
        new ContextVar(0.toByte, updatedRegistrarsMapResult.proof.ergoValue)
      )

      val registryOutBox =
        RegistryBoxBuilder()
          .withRegistrarsMap(registrarsMap)
          .withScript(defaultScript) // different script
          .build()
      val newRegistrarOutBox = NewRegistrarBoxBuilder().build()

      val tx = NewRegistrarTransaction()
        .withRegistryIn(registryInBox)
        .withNewRegistrarIn(newRegistrarInBox)
        .withRegistryOut(registryOutBox)
        .withNewRegistrarOut(newRegistrarOutBox)
        .build()

      (the[InterpreterException] thrownBy prover.sign(tx)).getMessage should be(
        "Script reduced to false"
      )
    })
  }

  "NewRegistrar" should "add new registrar to Registry" in {
    ergoClient.execute(implicit ctx => {
      val prover = ctx
        .newProverBuilder()
        .withDLogSecret(BigInt.apply(0).bigInteger)
        .build()

      val registrarsMap = defaultRegistryMap
      val tldToMint = "erg"

      val registryInBox =
        RegistryBoxBuilder()
          .withRegistrarsMap(registrarsMap)
          .withValue(200000000000000000L)
          .build()
          .convertToInputWith(fakeTxId3, fakeIndex)
      var newRegistrarInBox = NewRegistrarBoxBuilder()
        .withTld(tldToMint)
        .build()
        .convertToInputWith(fakeTxId3, fakeIndex)

      val hashedTld = Blake2b256.hash(tldToMint)
      val updatedRegistrarsMapResult = registrarsMap.insert((hashedTld, "01"))

      newRegistrarInBox = newRegistrarInBox.withContextVars(
        new ContextVar(0.toByte, updatedRegistrarsMapResult.proof.ergoValue)
      )

      val registryOutBox =
        RegistryBoxBuilder().withRegistrarsMap(registrarsMap).build()
      val newRegistrarOutBox = NewRegistrarBoxBuilder().build()

      val tx = NewRegistrarTransaction()
        .withRegistryIn(registryInBox)
        .withNewRegistrarIn(newRegistrarInBox)
        .withRegistryOut(registryOutBox)
        .withNewRegistrarOut(newRegistrarOutBox)
        .build()

      noException should be thrownBy prover.sign(tx)
    })
  }
}

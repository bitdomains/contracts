package org.bitdomains.contracts

import org.bitdomains.contracts.boxes.{
  NewRegistrarBoxBuilder,
  RegistryAdminBoxBuilder,
  RegistryBoxBuilder
}
import org.bitdomains.contracts.scenarios.ContractScenario
import org.bitdomains.contracts.transactions.NewRegistrarTransaction
import org.ergoplatform.appkit._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import scorex.crypto.hash.Blake2b256
import sigmastate.lang.exceptions.InterpreterException

case class NewRegistrarScenario(
    registrarsMap: RegistryState = defaultRegistryMap,
    tldToMint: String = "erg"
)(implicit
    ctx: BlockchainContext
) extends ContractScenario[NewRegistrarTransaction] {
  var registryIn: InputBox = RegistryBoxBuilder()
    .withRegistrarsMap(registrarsMap)
    .withValue(200000000000000000L)
    .build()
    .convertToInputWith(fakeTxId3, fakeIndex)

  var newRegistrarIn: InputBox = NewRegistrarBoxBuilder()
    .withTld(tldToMint)
    .build()
    .convertToInputWith(fakeTxId3, fakeIndex)

  var registryOut: OutBox =
    RegistryBoxBuilder().withRegistrarsMap(registrarsMap).build()

  // default value, should be updated during `insertTld` to get new tld value in output box
  var newRegistrarOut: OutBox = NewRegistrarBoxBuilder().build()

  def insertTld(tld: String = tldToMint, value: String = "01"): Unit = {
    val hashedTld = Blake2b256.hash(tld)
    val updatedRegistrarsMapResult = registrarsMap.insert((hashedTld, value))

    newRegistrarIn = newRegistrarIn.withContextVars(
      new ContextVar(0.toByte, updatedRegistrarsMapResult.proof.ergoValue)
    )
    registryOut = RegistryBoxBuilder()
      .withRegistrarsMap(registrarsMap)
      .build() // build outbox with registrar tld inserted
  }

  override def txBuilder: NewRegistrarTransaction = {
    NewRegistrarTransaction()
      .withRegistryIn(registryIn)
      .withNewRegistrarIn(newRegistrarIn)
      .withRegistryOut(registryOut)
      .withNewRegistrarOut(newRegistrarOut)
  }
}

class NewRegistrarSpec extends AnyFlatSpec with should.Matchers {
  "NewRegistrar" should "fail if RegistryAdmin input is missing" in {
    ergoClient.execute(implicit ctx => {
      val scenario = NewRegistrarScenario()

      scenario.insertTld()

      val tx = NewRegistrarTransaction()
        .partialBuild()
        .addInputs(Seq(scenario.registryIn, scenario.newRegistrarIn): _*)
        .addOutputs(Seq(scenario.registryOut, scenario.newRegistrarOut): _*)
        .build()

      an[ArrayIndexOutOfBoundsException] should be thrownBy scenario.prover
        .sign(tx)
    })
  }

  "NewRegistrar" should "fail if RegistryAdmin input has incorrect nft" in {
    ergoClient.execute(implicit ctx => {
      val scenario = NewRegistrarScenario()

      val registryAdmin = RegistryAdminBoxBuilder()
        .withNftId(
          "94af8793a1f7b427831dcb48368ffc55c68d319d525ea24510ac38b75e280a8e"
        )
        .build()

      scenario.insertTld()

      val builder = scenario.txBuilder
        .withAdminIn(
          registryAdmin.convertToInputWith(fakeTxId3, fakeIndex)
        )
        .withAdminOut(registryAdmin)

      (the[InterpreterException] thrownBy scenario.prover.sign(
        builder.build()
      )).getMessage should be(
        "Script reduced to false"
      )
    })
  }

  "NewRegistrar" should "fail if registrars state in Registry isn't updated correctly" in {
    ergoClient.execute(implicit ctx => {
      val scenario = NewRegistrarScenario(tldToMint = "ada")

      scenario.insertTld("erg") // different tld to boxes

      (the[InterpreterException] thrownBy scenario
        .mkAndSignTx()).getMessage should be(
        "Script reduced to false"
      )
    })
  }

  "NewRegistrar" should "fail if successor script changed" in {
    ergoClient.execute(implicit ctx => {
      val scenario = NewRegistrarScenario()

      scenario.insertTld()
      scenario.newRegistrarOut =
        NewRegistrarBoxBuilder().withScript(defaultScript).build()

      (the[InterpreterException] thrownBy scenario
        .mkAndSignTx()).getMessage should be(
        "Script reduced to false"
      )
    })
  }

  "NewRegistrar" should "add new registrar to Registry" in {
    ergoClient.execute(implicit ctx => {
      val scenario = NewRegistrarScenario()

      scenario.insertTld()

      noException should be thrownBy scenario.mkAndSignTx()
    })
  }
}

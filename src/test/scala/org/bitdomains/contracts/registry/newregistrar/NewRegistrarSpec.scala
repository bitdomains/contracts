package org.bitdomains.contracts.registry.newregistrar

import org.bitdomains.contracts.registry.RegistryAdminBoxBuilder
import org.bitdomains.contracts._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import sigmastate.lang.exceptions.InterpreterException

class NewRegistrarSpec extends AnyFlatSpec with should.Matchers {
  "NewRegistrar" should "fail if RegistryAdmin input is missing" in {
    ergoClient.execute(implicit ctx => {
      val scenario = NewRegistrarContractScenario()

      scenario.insertTld()

      val tx = NewRegistrarTransactionBuilder()
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
      val scenario = NewRegistrarContractScenario()

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
      val scenario = NewRegistrarContractScenario(tldToMint = "ada")

      scenario.insertTld("erg") // different tld to boxes

      (the[InterpreterException] thrownBy scenario
        .mkAndSignTx()).getMessage should be(
        "Script reduced to false"
      )
    })
  }

  "NewRegistrar" should "fail if successor script changed" in {
    ergoClient.execute(implicit ctx => {
      val scenario = NewRegistrarContractScenario()

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
      val scenario = NewRegistrarContractScenario()

      scenario.insertTld()

      noException should be thrownBy scenario.mkAndSignTx()
    })
  }
}

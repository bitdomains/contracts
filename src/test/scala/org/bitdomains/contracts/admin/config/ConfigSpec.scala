package org.bitdomains.contracts.admin.config

import org.bitdomains.contracts.{
  defaultScript,
  ergoClient,
  fakeIndex,
  fakeTxId3
}
import org.bitdomains.contracts.registry.RegistryAdminBoxBuilder
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import sigmastate.lang.exceptions.InterpreterException

class ConfigSpec extends AnyFlatSpec with should.Matchers {
  "Config" should "update tld state config" in {
    ergoClient.execute(implicit ctx => {
      val scenario = ConfigContractScenario()

      scenario.updateTldAction()

      noException should be thrownBy scenario.mkAndSignTx()
    })
  }

  "Config" should "fail if admin box input not present" in {
    ergoClient.execute(implicit ctx => {
      val scenario = ConfigContractScenario()

      scenario.updateTldAction()

      val tx = ConfigTransactionBuilder()
        .partialBuild()
        .addInputs(Seq(scenario.configIn): _*)
        .addOutputs(Seq(scenario.configOut): _*)
        .build()

      an[ArrayIndexOutOfBoundsException] should be thrownBy scenario.prover
        .sign(tx)
    })
  }

  "Config" should "fail if admin nft not valid" in {
    ergoClient.execute(implicit ctx => {
      val scenario = ConfigContractScenario()

      val adminBuilder =
        RegistryAdminBoxBuilder()
          .withNftId(
            "94af8793a1f7b427831dcb48368ffc55c68d319d525ea24510ac38b75e280a8e"
          )

      scenario.adminIn = adminBuilder
        .build()
        .convertToInputWith(fakeTxId3, fakeIndex)
      scenario.adminOut = adminBuilder
        .build()

      scenario.updateTldAction()

      (the[InterpreterException] thrownBy scenario
        .mkAndSignTx()).getMessage should be(
        "Script reduced to false"
      )
    })
  }

  "Config" should "fail if successor propositionBytes changed" in {
    ergoClient.execute(implicit ctx => {
      val scenario = ConfigContractScenario()

      scenario.updateTldAction()

      scenario.configOut = ConfigBoxBuilder()
        .withTldState(scenario.tldState)
        .withScript(defaultScript)
        .build()

      (the[InterpreterException] thrownBy scenario
        .mkAndSignTx()).getMessage should be(
        "Script reduced to false"
      )
    })
  }
}

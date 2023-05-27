package org.bitdomains.contracts.admin.config

import org.bitdomains.contracts.{
  WithBlockchainContext,
  defaultScript,
  randomErgoId
}
import org.ergoplatform.appkit.ErgoToken
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import sigmastate.lang.exceptions.InterpreterException

class ConfigSpec
    extends AnyFlatSpec
    with should.Matchers
    with WithBlockchainContext {
  it should "update tld state config" in {
    withBlockchain { implicit ctx =>
      val scenario = ConfigContractScenario()

      noException should be thrownBy scenario.mkAndSignTx()
    }
  }

  "isAdmin" should "fail with invalid admin box" in {
    withBlockchain { implicit ctx =>
      val scenario = ConfigContractScenario()

      scenario.adminIn.withNftId(
        "94af8793a1f7b427831dcb48368ffc55c68d319d525ea24510ac38b75e280a8f"
      )
      scenario.adminOut.withNftId(
        "94af8793a1f7b427831dcb48368ffc55c68d319d525ea24510ac38b75e280a8f"
      )

      (the[InterpreterException] thrownBy scenario
        .mkAndSignTx()).getMessage should be(
        "Script reduced to false"
      )
    }
  }

  "validSuccessor" should "fail if propositionBytes changed" in {
    withBlockchain { implicit ctx =>
      val scenario = ConfigContractScenario()

      scenario.configOut = scenario.configOut.withScript(defaultScript)

      (the[InterpreterException] thrownBy scenario
        .mkAndSignTx()).getMessage should be(
        "Script reduced to false"
      )
    }
  }

  "validSuccessor" should "fail if nft changed" in {
    withBlockchain { implicit ctx =>
      val scenario = ConfigContractScenario()

      val extraNftId = randomErgoId

      scenario.adminIn.withTokens(new ErgoToken(extraNftId, 2))
      scenario.adminOut.withTokens(new ErgoToken(extraNftId, 1))
      scenario.configOut = scenario.configOut.withNftId(extraNftId)

      (the[InterpreterException] thrownBy scenario
        .mkAndSignTx()).getMessage should be(
        "Script reduced to false"
      )
    }
  }
}

package org.bitdomains.contracts.admin.config

import org.bitdomains.contracts.{
  WithBlockchainContext,
  defaultScript,
  randomErgoId
}
import org.ergoplatform.appkit.ErgoToken
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should
import sigmastate.lang.exceptions.InterpreterException

class ConfigSpec
    extends AnyFunSpec
    with should.Matchers
    with WithBlockchainContext {
  describe("Config") {
    describe("Global checks") {
      describe("isAdmin") {
        it("should fail with invalid admin box") {
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
      }

      describe("validSuccessor") {
        it("should fail if propositionBytes changed") {
          withBlockchain { implicit ctx =>
            val scenario = ConfigContractScenario()

            scenario.configOut.withScript(defaultScript)

            (the[InterpreterException] thrownBy scenario
              .mkAndSignTx()).getMessage should be(
              "Script reduced to false"
            )
          }
        }

        it("should fail if nft changed") {
          withBlockchain { implicit ctx =>
            val scenario = ConfigContractScenario()

            val extraNftId = randomErgoId

            scenario.adminIn.withTokens(new ErgoToken(extraNftId, 2))
            scenario.adminOut.withTokens(new ErgoToken(extraNftId, 1))
            scenario.configOut.withNftId(extraNftId)

            (the[InterpreterException] thrownBy scenario
              .mkAndSignTx()).getMessage should be(
              "Script reduced to false"
            )
          }
        }
      }
    }

    describe("ActionUpdateRegistrars") {
      it("should update registrar state config") {
        withBlockchain { implicit ctx =>
          val scenario = ConfigContractScenario()

          noException should be thrownBy scenario.mkAndSignTx()
        }
      }

      it("should fail if script hashes config is changed") {
        withBlockchain { implicit ctx =>
          val scenario = ConfigContractScenario()

          scenario.configOut.withResolverHash(defaultScript.getBytes)

          (the[InterpreterException] thrownBy scenario
            .mkAndSignTx()).getMessage should be(
            "Script reduced to false"
          )
        }
      }
    }
  }
}

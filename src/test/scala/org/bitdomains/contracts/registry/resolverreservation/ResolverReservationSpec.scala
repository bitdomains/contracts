package org.bitdomains.contracts.registry.resolverreservation

import org.bitdomains.contracts.{WithBlockchainContext, randomErgoId}
import org.ergoplatform.appkit.{ErgoToken, JavaHelpers, SecretString}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import scorex.crypto.hash.Blake2b256
import sigmastate.eval.CostingSigmaDslBuilder.GroupElement
import sigmastate.lang.exceptions.InterpreterException

class ResolverReservationSpec
    extends AnyFlatSpec
    with should.Matchers
    with WithBlockchainContext {
  it should "mint ReservedResolver box" in {
    withBlockchain { implicit ctx =>
      val scenario = ResolverReservationContractScenario()

      noException should be thrownBy scenario.mkAndSignTx()
    }
  }

  "validRegistryInBox" should "fail with invalid registry box" in {
    withBlockchain { implicit ctx =>
      val scenario = ResolverReservationContractScenario()

      scenario.registryIn.withNftId(
        "94af8793a1f7b427831dcb48368ffc55c68d319d525ea24510ac38b75e280a8f"
      )
      scenario.registryOut.withNftId(
        "94af8793a1f7b427831dcb48368ffc55c68d319d525ea24510ac38b75e280a8f"
      )

      (the[InterpreterException] thrownBy scenario
        .mkAndSignTx()).getMessage should be(
        "Script reduced to false"
      )
    }
  }

  "validReservedResolverBox" should "fail if `hashedResolver` mismatch" in {
    withBlockchain { implicit ctx =>
      val scenario =
        ResolverReservationContractScenario(requestLabel = "requestname")

      scenario.reservedResolverOut.withHashedReservation(
        Blake2b256("differentname" ++ "erg")
      )

      (the[InterpreterException] thrownBy scenario
        .mkAndSignTx()).getMessage should be(
        "Script reduced to false"
      )
    }
  }

  "validReservedResolverBox" should "fail if `resolveAddress` mismatch" in {
    withBlockchain { implicit ctx =>
      val scenario = ResolverReservationContractScenario()

      // sigmaProp(false)
      scenario.reservedResolverOut.withResolveAddress("4MQyMKvMbnCJG3aJ")

      (the[InterpreterException] thrownBy scenario
        .mkAndSignTx()).getMessage should be(
        "Script reduced to false"
      )
    }
  }

  "validReservedResolverBox" should "fail if `buyerPk` mismatch" in {
    withBlockchain { implicit ctx =>
      val scenario = ResolverReservationContractScenario()

      val mnemonic = SecretString.create("different mnemonic for different key")
      val rootSecret =
        JavaHelpers.seedToMasterKey(mnemonic, SecretString.empty(), true)
      val path = JavaHelpers.eip3DerivationParent
      val sk = rootSecret.derive(path)

      scenario.reservedResolverOut.withBuyerPk(
        GroupElement(sk.publicImage.value)
      )

      (the[InterpreterException] thrownBy scenario
        .mkAndSignTx()).getMessage should be(
        "Script reduced to false"
      )
    }
  }

  "validReservedResolverBox" should "fail if `nft` mismatch" in {
    withBlockchain { implicit ctx =>
      val scenario = ResolverReservationContractScenario()

      val extraNftId = randomErgoId

      scenario.resolverReservationIn.withTokens(new ErgoToken(extraNftId, 2))
      scenario.resolverReservationOut.withTokens(new ErgoToken(extraNftId, 1))
      scenario.reservedResolverOutNftOverride = Some(extraNftId)

      (the[InterpreterException] thrownBy scenario
        .mkAndSignTx()).getMessage should be(
        "Script reduced to false"
      )
    }
  }

  "validReservedResolverBox" should "fail if `nft` amount != 1" in {
    withBlockchain { implicit ctx =>
      val scenario = ResolverReservationContractScenario()

      scenario.reservedResolverOutNftAmount = 2

      (the[InterpreterException] thrownBy scenario
        .mkAndSignTx()).getMessage should be(
        "Script reduced to false"
      )
    }
  }
}

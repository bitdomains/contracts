package org.bitdomains.contracts.registry.mintresolver

import org.bitdomains.contracts.{
  WithBlockchainContext,
  defaultScript,
  randomErgoId,
  randomProverInput
}
import org.ergoplatform.appkit.{ErgoToken, TokenBalanceException}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import scorex.crypto.hash.Blake2b256
import sigmastate.eval.CostingSigmaDslBuilder.GroupElement
import sigmastate.lang.exceptions.InterpreterException

class MintResolverSpec
    extends AnyFlatSpec
    with should.Matchers
    with WithBlockchainContext {
  it should "mint Resolver box" in {
    withBlockchain { implicit ctx =>
      val scenario = MintResolverContractScenario()

      noException should be thrownBy scenario.mkAndSignTx()
    }
  }

  "validConfigBox" should "fail with incorrect nft" in {
    withBlockchain { implicit ctx =>
      val scenario = MintResolverContractScenario()

      scenario.configDataIn.withNftId(randomErgoId)

      (the[InterpreterException] thrownBy scenario
        .mkAndSignTx()).getMessage should be(
        "Script reduced to false"
      )
    }
  }

  "validRegistryInBox" should "fail with incorrect nft" in {
    withBlockchain { implicit ctx =>
      val scenario = MintResolverContractScenario()

      scenario.configDataIn.withNftId(randomErgoId)

      (the[InterpreterException] thrownBy scenario
        .mkAndSignTx()).getMessage should be(
        "Script reduced to false"
      )
    }
  }

  "validSuccessorBox" should "fail if script bytes changed" in {
    withBlockchain { implicit ctx =>
      val scenario = MintResolverContractScenario()

      scenario.mintResolverOut.withScript(defaultScript)

      (the[InterpreterException] thrownBy scenario
        .mkAndSignTx()).getMessage should be(
        "Script reduced to false"
      )
    }
  }

  "validSuccessorBox" should "fail if nft changed" in {
    withBlockchain { implicit ctx =>
      val scenario = MintResolverContractScenario()

      val extraNftId = randomErgoId

      scenario.mintResolverRequestIn.withTokens(new ErgoToken(extraNftId, 2))
      scenario.resolverOut.withTokens(new ErgoToken(extraNftId, 1))
      scenario.mintResolverOut.withNftId(extraNftId)

      (the[InterpreterException] thrownBy scenario
        .mkAndSignTx()).getMessage should be(
        "Script reduced to false"
      )
    }
  }

  "validResolverBox" should "fail if script bytes changed" in {
    withBlockchain { implicit ctx =>
      val scenario = MintResolverContractScenario()

      scenario.resolverOut.withScript(defaultScript)

      (the[InterpreterException] thrownBy scenario
        .mkAndSignTx()).getMessage should be(
        "Script reduced to false"
      )
    }
  }

  "validResolverBox" should "fail if incorrect nft" in {
    withBlockchain { implicit ctx =>
      val scenario = MintResolverContractScenario()

      val extraNftId = randomErgoId

      scenario.mintResolverRequestIn.withTokens(new ErgoToken(extraNftId, 2))
      scenario.resolverOut.withTokens(new ErgoToken(extraNftId, 2))
      scenario.overrideNewResolverNft = Some(extraNftId)

      (the[TokenBalanceException] thrownBy scenario
        .mkAndSignTx()).getMessage should include(
        "Cannot mint a token with invalid id"
      )
    }
  }

  "validResolverBox" should "fail if nft balance > 1" in {
    withBlockchain { implicit ctx =>
      val scenario = MintResolverContractScenario()

      scenario.resolverOutNftAmount = 2

      (the[InterpreterException] thrownBy scenario
        .mkAndSignTx()).getMessage should be(
        "Script reduced to false"
      )
    }
  }

  "validResolverBox" should "fail if buyerPk mismatch" in {
    withBlockchain { implicit ctx =>
      val scenario = MintResolverContractScenario()

      scenario.resolverOut.withOwnerPk(
        GroupElement(randomProverInput.publicImage.value)
      )

      (the[InterpreterException] thrownBy scenario
        .mkAndSignTx()).getMessage should be(
        "Script reduced to false"
      )
    }
  }

  "validResolverBox" should "fail if label mismatch" in {
    withBlockchain { implicit ctx =>
      val scenario = MintResolverContractScenario()

      scenario.resolverOut.withLabel("fail")

      (the[InterpreterException] thrownBy scenario
        .mkAndSignTx()).getMessage should be(
        "Script reduced to false"
      )
    }
  }

  "validResolverBox" should "fail if tld mismatch" in {
    withBlockchain { implicit ctx =>
      val scenario = MintResolverContractScenario()

      scenario.resolverOut.withTld("ada")

      (the[InterpreterException] thrownBy scenario
        .mkAndSignTx()).getMessage should be(
        "Script reduced to false"
      )
    }
  }

  "validResolverBox" should "fail if resolveAddress mismatch" in {
    withBlockchain { implicit ctx =>
      val scenario = MintResolverContractScenario()

      // sigmaProp(false)
      scenario.resolverOut.withAddress("4MQyMKvMbnCJG3aJ")

      (the[InterpreterException] thrownBy scenario
        .mkAndSignTx()).getMessage should be(
        "Script reduced to false"
      )
    }
  }

  "validLabel" should "fail if label too short" in {
    withBlockchain { implicit ctx =>
      val scenario = MintResolverContractScenario(existingReservedLabel = "a")

      (the[InterpreterException] thrownBy scenario
        .mkAndSignTx()).getMessage should be(
        "Script reduced to false"
      )
    }
  }

  "validLabel" should "fail if label too long" in {
    withBlockchain { implicit ctx =>
      val scenario = MintResolverContractScenario(existingReservedLabel =
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
      )

      (the[InterpreterException] thrownBy scenario
        .mkAndSignTx()).getMessage should be(
        "Script reduced to false"
      )
    }
  }

  "validTld" should "fail if tld doesn't exist" in {
    withBlockchain { implicit ctx =>
      val scenario =
        MintResolverContractScenario(overrideExistingTldState = Some("ada"))

      (the[InterpreterException] thrownBy scenario
        .mkAndSignTx()).getMessage should be(
        "Script reduced to false"
      )
    }
  }

  "validReservation" should "fail if nft wasn't the same as nft in reservations state" in {
    withBlockchain { implicit ctx =>
      val scenario =
        MintResolverContractScenario(overrideExistingReservationNft =
          Some(randomErgoId)
        )

      (the[InterpreterException] thrownBy scenario
        .mkAndSignTx()).getMessage should be(
        "Script reduced to false"
      )
    }
  }

  "validReservation" should "fail if hashedResolver mismatch" in {
    withBlockchain { implicit ctx =>
      val scenario = MintResolverContractScenario()

      scenario.reservedResolverIn.withHashedReservation(Blake2b256("incorrect"))

      (the[InterpreterException] thrownBy scenario
        .mkAndSignTx()).getMessage should be(
        "Script reduced to false"
      )
    }
  }

  "validReservation" should "fail if buyerPk mismatch" in {
    withBlockchain { implicit ctx =>
      val scenario = MintResolverContractScenario()

      scenario.reservedResolverIn.withBuyerPk(
        GroupElement(randomProverInput.publicImage.value)
      )

      (the[InterpreterException] thrownBy scenario
        .mkAndSignTx()).getMessage should be(
        "Script reduced to false"
      )
    }
  }

  "validReservation" should "fail if resolveAddress mismatch" in {
    withBlockchain { implicit ctx =>
      val scenario = MintResolverContractScenario()

      scenario.reservedResolverIn.withResolveAddress("4MQyMKvMbnCJG3aJ")

      (the[InterpreterException] thrownBy scenario
        .mkAndSignTx()).getMessage should be(
        "Script reduced to false"
      )
    }
  }
}

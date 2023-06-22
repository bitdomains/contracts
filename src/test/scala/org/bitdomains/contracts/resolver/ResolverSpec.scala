package org.bitdomains.contracts.resolver

import org.bitdomains.contracts.{
  WithBlockchainContext,
  randomErgoId,
  randomProverInput
}
import org.ergoplatform.appkit.SigmaProp
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import sigmastate.lang.exceptions.InterpreterException

class ResolverSpec
    extends AnyFlatSpec
    with should.Matchers
    with WithBlockchainContext {
  it should "be possible to update resolved address" in {
    withBlockchain { implicit ctx =>
      val scenario = ResolverContractScenario()
      val proverInput = randomProverInput

      scenario.prover =
        ctx.newProverBuilder().withDLogSecret(proverInput.w).build()
      scenario.resolverIn.withOwnerProp(
        new SigmaProp(proverInput.publicImage)
      )
      scenario.resolverOut.withOwnerProp(
        new SigmaProp(proverInput.publicImage)
      )
      scenario.resolverOut.withAddress("9f5y5JY6JQZ6Q5Q")

      noException should be thrownBy scenario.mkAndSignTx()
    }
  }

  it should "be possible to update ownerPk" in {
    withBlockchain { implicit ctx =>
      val scenario = ResolverContractScenario()
      scenario.action = TransferOwnershipAction
      val proverInput = randomProverInput

      scenario.prover =
        ctx.newProverBuilder().withDLogSecret(proverInput.w).build()
      scenario.resolverIn.withOwnerProp(
        new SigmaProp(proverInput.publicImage)
      )

      noException should be thrownBy scenario.mkAndSignTx()
    }
  }

  "validNft" should "fail if nft changed" in {
    withBlockchain { implicit ctx =>
      val scenario = ResolverContractScenario()

      scenario.resolverOut.withNftId(randomErgoId)

      (the[Exception] thrownBy scenario
        .mkAndSignTx()).getMessage should include(
        "Not enough tokens"
      )
    }
  }

  "isOwner" should "fail if tx not signed with prover input" in {
    withBlockchain { implicit ctx =>
      val scenario = ResolverContractScenario()
      scenario.action = TransferOwnershipAction
      val proverInput = randomProverInput

      scenario.resolverOut.withOwnerProp(
        new SigmaProp(proverInput.publicImage)
      )

      (the[AssertionError] thrownBy scenario
        .mkAndSignTx()).getMessage should include(
        "Tree root should be real but was UnprovenSchnorr"
      )
    }
  }

  "validLabel" should "fail if label changed" in {
    withBlockchain { implicit ctx =>
      val scenario = ResolverContractScenario()
      scenario.action = TransferOwnershipAction
      val proverInput = randomProverInput

      scenario.prover =
        ctx.newProverBuilder().withDLogSecret(proverInput.w).build()
      scenario.resolverIn.withOwnerProp(
        new SigmaProp(proverInput.publicImage)
      )

      scenario.resolverOut.withLabel("differentname")

      (the[InterpreterException] thrownBy scenario
        .mkAndSignTx()).getMessage should be(
        "Script reduced to false"
      )
    }
  }

  "validTld" should "fail if tld changed" in {
    withBlockchain { implicit ctx =>
      val scenario = ResolverContractScenario()
      scenario.action = TransferOwnershipAction
      val proverInput = randomProverInput

      scenario.prover =
        ctx.newProverBuilder().withDLogSecret(proverInput.w).build()
      scenario.resolverIn.withOwnerProp(
        new SigmaProp(proverInput.publicImage)
      )

      scenario.resolverOut.withTld("ada")

      (the[InterpreterException] thrownBy scenario
        .mkAndSignTx()).getMessage should be(
        "Script reduced to false"
      )
    }
  }

  "validScript" should "fail if script changed" in {
    withBlockchain { implicit ctx =>
      val scenario = ResolverContractScenario()
      scenario.action = TransferOwnershipAction
      val proverInput = randomProverInput

      scenario.prover =
        ctx.newProverBuilder().withDLogSecret(proverInput.w).build()
      scenario.resolverIn.withOwnerProp(
        new SigmaProp(proverInput.publicImage)
      )

      scenario.resolverOut.withScript("sigmaProp(true && true)")

      (the[InterpreterException] thrownBy scenario
        .mkAndSignTx()).getMessage should be(
        "Script reduced to false"
      )
    }
  }
}

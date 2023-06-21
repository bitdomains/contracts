package org.bitdomains.contracts.reserveresolverrequest

import org.bitdomains.contracts.WithBlockchainContext
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class ReserveResolverRequestSpec
    extends AnyFlatSpec
    with should.Matchers
    with WithBlockchainContext {
  it should "be spendable by the buyer" in {
    withBlockchain { implicit ctx =>
      val scenario = ReserveResolverRequestContractScenario()

      noException should be thrownBy scenario.mkAndSignTx()
    }
  }

  it should "fail if the buyer sigma proposition is not satisfied" in {
    withBlockchain { implicit ctx =>
      val scenario = ReserveResolverRequestContractScenario()

      scenario.prover = ctx
        .newProverBuilder()
        .withDLogSecret(BigInt.apply(0).bigInteger)
        .build()

      (the[AssertionError] thrownBy scenario
        .mkAndSignTx()).getMessage should include(
        "Tree root should be real but was UnprovenSchnorr"
      )
    }
  }
}

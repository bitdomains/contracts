package org.bitdomains.contracts.registry.mintresolver

import org.bitdomains.contracts.WithBlockchainContext
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

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
}

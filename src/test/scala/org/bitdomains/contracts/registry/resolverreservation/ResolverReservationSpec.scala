package org.bitdomains.contracts.registry.resolverreservation

import org.bitdomains.contracts.ergoClient
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class ResolverReservationSpec extends AnyFlatSpec with should.Matchers {
  "ResolverReservation" should "mint ReservedResolver box" in {
    ergoClient.execute(implicit ctx => {
      val scenario = ResolverReservationContractScenario()

      scenario.doAvlOps()

      noException should be thrownBy scenario.mkAndSignTx()
    })
  }
}

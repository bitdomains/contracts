package org.bitdomains.contracts.reserveresolverrequest

import org.bitdomains.contracts.utils.scenarios.ContractScenario
import org.bitdomains.contracts.{defaultScript, fakeIndex, fakeTxId1}
import org.ergoplatform.appkit.{
  BlockchainContext,
  ConstantsBuilder,
  OutBoxBuilder
}

case class ReserveResolverRequestContractScenario(implicit
    ctx: BlockchainContext
) extends ContractScenario[ReserveResolverRequestTransactionBuilder] {
  var reserveResolverRequestIn: ReserveResolverRequestBoxBuilder =
    ReserveResolverRequestBoxBuilder()

  var refundOut: OutBoxBuilder = ctx
    .newTxBuilder()
    .outBoxBuilder()
    .contract(ctx.compileContract(ConstantsBuilder.empty(), defaultScript))

  override def txBuilder: ReserveResolverRequestTransactionBuilder = {
    val refundOutBox =
      refundOut.value(reserveResolverRequestIn.value - 10000000).build()

    ReserveResolverRequestTransactionBuilder()
      .withReserveResolverRequestIn(
        reserveResolverRequestIn
          .build()
          .convertToInputWith(fakeTxId1, fakeIndex)
      )
      .withRefundOut(refundOutBox)
  }
}

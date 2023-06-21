package org.bitdomains.contracts.mintresolverrequest

import org.bitdomains.contracts.{defaultScript, fakeIndex, fakeTxId1}
import org.bitdomains.contracts.utils.scenarios.ContractScenario
import org.ergoplatform.appkit.{
  BlockchainContext,
  ConstantsBuilder,
  OutBoxBuilder
}

case class MintResolverRequestContractScenario(implicit
    ctx: BlockchainContext
) extends ContractScenario[MintResolverRequestTransactionBuilder] {
  var mintResolverRequestIn: MintResolverRequestBoxBuilder =
    MintResolverRequestBoxBuilder()

  var refundOut: OutBoxBuilder = ctx
    .newTxBuilder()
    .outBoxBuilder()
    .contract(ctx.compileContract(ConstantsBuilder.empty(), defaultScript))

  override def txBuilder: MintResolverRequestTransactionBuilder = {
    // TODO: value - miner fee
    val refundOutBox =
      refundOut.value(mintResolverRequestIn.value - 10000000).build()

    MintResolverRequestTransactionBuilder()
      .withMintResolverRequestIn(
        mintResolverRequestIn.build().convertToInputWith(fakeTxId1, fakeIndex)
      )
      .withRefundOut(refundOutBox)
  }
}

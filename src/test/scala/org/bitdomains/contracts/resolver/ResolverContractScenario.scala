package org.bitdomains.contracts.resolver

import org.bitdomains.contracts.{
  fakeIndex,
  fakeTxId1,
  randomErgoId,
  randomProverInput
}
import org.bitdomains.contracts.utils.scenarios.ContractScenario
import org.ergoplatform.appkit.{BlockchainContext, ContextVar, ErgoValue}
import sigmastate.eval.CostingSigmaDslBuilder.GroupElement

sealed trait ResolverAction {
  def id: Byte
}

case object UpdateResolveAddressAction extends ResolverAction {
  override def id: Byte = 1
}

case object TransferOwnershipAction extends ResolverAction {
  override def id: Byte = 2
}

case class ResolverContractScenario(implicit ctx: BlockchainContext)
    extends ContractScenario[ResolverTransactionBuilder] {
  private val defaultPk = GroupElement(randomProverInput.publicImage.value)
  private val defaultLabel = "mename"
  private val defaultTld = "erg"
  private val defaultAddress = "9f5y5JY6J8Z6Q5Q"
  private val defaultNft = randomErgoId

  var action: ResolverAction = UpdateResolveAddressAction

  var resolverIn: ResolverBoxBuilder = ResolverBoxBuilder()
    .withValue(1000000000000000000L)
    .withNftId(defaultNft)
    .withLabel(defaultLabel)
    .withTld(defaultTld)
    .withOwnerPk(defaultPk)
    .withAddress(defaultAddress)

  var resolverOut: ResolverBoxBuilder = ResolverBoxBuilder()
    .withNftId(defaultNft)
    .withLabel(defaultLabel)
    .withTld(defaultTld)
    .withOwnerPk(defaultPk)
    .withAddress(defaultAddress)

  override def txBuilder: ResolverTransactionBuilder = {
    ResolverTransactionBuilder()
      .withResolverIn(
        resolverIn
          .build()
          .convertToInputWith(fakeTxId1, fakeIndex)
          .withContextVars(new ContextVar(0, ErgoValue.of(action.id)))
      )
      .withResolverOut(resolverOut.build())
  }
}

package org.bitdomains.contracts.resolver

import bitdomains.Constants.resolverScript
import org.bitdomains.contracts.utils.builders.BoxBuilder
import org.bitdomains.contracts.walletSk
import org.ergoplatform.appkit.{BlockchainContext, ErgoValue, OutBox}
import sigmastate.eval.CostingSigmaDslBuilder.GroupElement
import special.sigma.GroupElement

case class ResolverBoxBuilder(implicit ctx: BlockchainContext)
    extends BoxBuilder(resolverScript) {
  private var buyerPk: GroupElement = GroupElement(walletSk.publicImage.value)

  private var label: String = ""

  private var tld: String = ""

  private var resolveAddress: String = "4MQyML64GnzMxZgm"

  def withBuyerPk(buyerPk: GroupElement): this.type = {
    this.buyerPk = buyerPk
    this
  }

  def withLabel(v: String): this.type = {
    this.label = v
    this
  }

  def withTld(v: String): this.type = {
    this.tld = v
    this
  }

  def withAddress(v: String): this.type = {
    this.resolveAddress = v
    this
  }

  override def build(): OutBox = {
    this
      .partialBuild()
      .registers(
        ErgoValue.of(buyerPk),
        ErgoValue.of(label.getBytes),
        ErgoValue.of(tld.getBytes),
        ErgoValue.of(resolveAddress.getBytes)
      )
      .build()
  }
}

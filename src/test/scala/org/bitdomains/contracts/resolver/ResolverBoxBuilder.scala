package org.bitdomains.contracts.resolver

import bitdomains.Constants.resolverScript
import org.bitdomains.contracts.utils.builders.BoxBuilder
import org.bitdomains.contracts.{RegistryState, defaultRegistryMap, walletSk}
import org.ergoplatform.appkit.{BlockchainContext, ErgoValue, OutBox, SigmaProp}

case class ResolverBoxBuilder(implicit ctx: BlockchainContext)
    extends BoxBuilder(resolverScript) {
  private var ownerProp: SigmaProp = new SigmaProp(walletSk.publicImage)

  private var label: String = ""

  private var tld: String = ""

  private var resolveAddress: String = "4MQyML64GnzMxZgm"

  private var subResolvers = defaultRegistryMap

  def withOwnerProp(ownerProp: SigmaProp): this.type = {
    this.ownerProp = ownerProp
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

  def withSubResolvers(v: RegistryState): this.type = {
    this.subResolvers = v
    this
  }

  override def build(): OutBox = {
    this
      .partialBuild()
      .registers(
        ErgoValue.of(ownerProp),
        ErgoValue.of(label.getBytes),
        ErgoValue.of(tld.getBytes),
        ErgoValue.of(resolveAddress.getBytes),
        subResolvers.ergoValue
      )
      .build()
  }
}

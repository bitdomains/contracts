package org.bitdomains.contracts.resolver

import bitdomains.Constants.resolverScript
import org.bitdomains.contracts.utils.builders.BoxBuilder
import org.bitdomains.contracts.walletSk
import org.ergoplatform.appkit.scalaapi.ErgoValueBuilder
import org.ergoplatform.appkit.{BlockchainContext, ErgoValue, OutBox, SigmaProp}
import sigmastate.eval.SigmaDsl

case class ResolverBoxBuilder(implicit ctx: BlockchainContext)
    extends BoxBuilder(resolverScript) {
  private var ownerProp: SigmaProp = new SigmaProp(walletSk.publicImage)

  private var label: String = ""

  private var tld: String = ""

  private var resolveAddress: String = "4MQyML64GnzMxZgm"

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

  override def build(): OutBox = {
    val labels = Array(label.getBytes, tld.getBytes)
    val labelsColl =
      SigmaDsl.Colls.fromArray(labels.map(SigmaDsl.Colls.fromArray(_)))

    this
      .partialBuild()
      .registers(
        ErgoValue.of(ownerProp),
        ErgoValueBuilder.buildFor(labelsColl),
        ErgoValue.of(resolveAddress.getBytes)
      )
      .build()
  }
}

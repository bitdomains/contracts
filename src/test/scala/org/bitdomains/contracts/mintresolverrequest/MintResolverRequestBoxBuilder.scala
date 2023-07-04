package org.bitdomains.contracts.mintresolverrequest

import bitdomains.Constants.mintResolverRequestScript
import org.bitdomains.contracts.utils.builders.BoxBuilder
import org.bitdomains.contracts.{hexToBytes, walletSk}
import org.ergoplatform.appkit.scalaapi.ErgoValueBuilder
import org.ergoplatform.appkit.{BlockchainContext, ErgoValue, OutBox, SigmaProp}
import sigmastate.eval.CostingSigmaDslBuilder.GroupElement
import sigmastate.eval.SigmaDsl
import special.sigma.GroupElement

case class MintResolverRequestBoxBuilder(implicit ctx: BlockchainContext)
    extends BoxBuilder(mintResolverRequestScript) {
  private var reservedResolverNftId: String = ""

  private var buyerProp: SigmaProp = new SigmaProp(walletSk.publicImage)

  private var label: String = ""

  private var tld: String = ""

  private var resolveAddress: String = "4MQyML64GnzMxZgm"

  def withReservedResolverNftId(v: String): this.type = {
    this.reservedResolverNftId = v
    this
  }

  def withBuyerProp(buyerProp: SigmaProp): this.type = {
    this.buyerProp = buyerProp
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
        ErgoValue.of(hexToBytes(reservedResolverNftId)),
        ErgoValue.of(buyerProp),
        ErgoValueBuilder.buildFor(labelsColl),
        ErgoValue.of(resolveAddress.getBytes)
      )
      .build()
  }
}

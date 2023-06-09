package org.bitdomains.contracts.reserveresolverrequest

import bitdomains.Constants.reserveResolverRequestScript
import org.bitdomains.contracts.utils.builders.BoxBuilder
import org.bitdomains.contracts.walletSk
import org.ergoplatform.appkit.{BlockchainContext, ErgoValue, OutBox, SigmaProp}
import scorex.crypto.hash.Blake2b256
import sigmastate.eval.CostingSigmaDslBuilder.GroupElement
import special.sigma.GroupElement

case class ReserveResolverRequestBoxBuilder(implicit ctx: BlockchainContext)
    extends BoxBuilder(reserveResolverRequestScript) {
  private var hashedReservation: Array[Byte] =
    Blake2b256.hash("myname" ++ "erg") // label ++ tld

  private var buyerProp: SigmaProp = new SigmaProp(walletSk.publicImage)

  private var resolveAddress: String = "4MQyML64GnzMxZgm"

  def withHashedReservation(hash: Array[Byte]): this.type = {
    hashedReservation = hash
    this
  }

  def withBuyerProp(buyerProp: SigmaProp): this.type = {
    this.buyerProp = buyerProp
    this
  }

  def withResolveAddress(resolveAddress: String): this.type = {
    this.resolveAddress = resolveAddress
    this
  }

  override def build(): OutBox = {
    this
      .partialBuild()
      .registers(
        ErgoValue.of(hashedReservation),
        ErgoValue.of(buyerProp),
        ErgoValue.of(resolveAddress.getBytes)
      )
      .build()
  }
}

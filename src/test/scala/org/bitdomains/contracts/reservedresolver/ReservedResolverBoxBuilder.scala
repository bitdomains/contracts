package org.bitdomains.contracts.reservedresolver

import bitdomains.Constants.reservedResolverScript
import org.bitdomains.contracts.utils.builders.BoxBuilder
import org.bitdomains.contracts.walletSk
import org.ergoplatform.appkit.{BlockchainContext, ErgoValue, OutBox}
import scorex.crypto.hash.Blake2b256
import sigmastate.eval.CostingSigmaDslBuilder.GroupElement
import special.sigma.GroupElement

case class ReservedResolverBoxBuilder(implicit ctx: BlockchainContext)
    extends BoxBuilder(reservedResolverScript) {
  private var hashedReservation: Array[Byte] =
    Blake2b256("myname" ++ "erg") // label ++ tld

  private var buyerPk: GroupElement = GroupElement(walletSk.publicImage.value)

  private var resolveAddress: String = "4MQyML64GnzMxZgm"

  def withHashedReservation(hash: Array[Byte]): this.type = {
    hashedReservation = hash
    this
  }

  def withBuyerPk(buyerPk: GroupElement): this.type = {
    this.buyerPk = buyerPk
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
        ErgoValue.of(buyerPk),
        ErgoValue.of(resolveAddress.getBytes)
      )
      .build()
  }
}

package org.bitdomains.contracts.boxes

import bitdomains.Constants.{newRegistrarNft, newRegistrarScript}
import org.bitdomains.contracts.builders.BoxBuilder
import org.ergoplatform.appkit.{BlockchainContext, ErgoValue, OutBox}

case class NewRegistrarBoxBuilder(implicit ctx: BlockchainContext)
    extends BoxBuilder(newRegistrarScript, newRegistrarNft) {
  private var tld: String = "erg"

  def withTld(tld: String): NewRegistrarBoxBuilder = {
    this.tld = tld
    this
  }

  override def build(): OutBox = {
    this.partialBuild().registers(ErgoValue.of(this.tld.getBytes)).build()
  }
}

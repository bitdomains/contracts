package org.bitdomains.contracts.boxes

import bitdomains.Constants.registryAdminNft
import org.bitdomains.contracts.builders.BoxBuilder
import org.bitdomains.contracts.defaultScript
import org.ergoplatform.appkit.{BlockchainContext, ErgoValue, OutBox}

case class RegistryAdminBoxBuilder(implicit ctx: BlockchainContext)
    extends BoxBuilder(defaultScript, registryAdminNft) {
  override def build(): OutBox = {
    this.partialBuild().build()
  }
}

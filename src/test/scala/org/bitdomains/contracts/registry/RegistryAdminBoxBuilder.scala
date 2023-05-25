package org.bitdomains.contracts.registry

import bitdomains.Constants.adminNft
import org.bitdomains.contracts.defaultScript
import org.bitdomains.contracts.utils.builders.BoxBuilder
import org.ergoplatform.appkit.{BlockchainContext, OutBox}

case class RegistryAdminBoxBuilder(implicit ctx: BlockchainContext)
    extends BoxBuilder(defaultScript, adminNft) {
  override def build(): OutBox = {
    this.partialBuild().build()
  }
}

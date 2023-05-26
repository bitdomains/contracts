package org.bitdomains.contracts.admin.config

import bitdomains.Constants.{configNfg, configScript}
import org.bitdomains.contracts.{RegistryState, defaultRegistryMap}
import org.bitdomains.contracts.utils.builders.BoxBuilder
import org.ergoplatform.appkit.{BlockchainContext, ErgoValue, OutBox}

case class ConfigBoxBuilder(implicit ctx: BlockchainContext)
    extends BoxBuilder(configScript, configNfg) {
  private var tldState: RegistryState = defaultRegistryMap

  def withTldState(v: RegistryState): this.type = {
    this.tldState = v
    this
  }

  override def build(): OutBox = {
    this.partialBuild().registers(tldState.ergoValue).build()
  }
}

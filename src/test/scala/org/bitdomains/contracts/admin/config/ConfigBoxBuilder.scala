package org.bitdomains.contracts.admin.config

import bitdomains.Constants.{
  configNft,
  configScript,
  reservedResolverScriptHash,
  resolverScriptHash
}
import org.bitdomains.contracts.utils.builders.BoxBuilder
import org.bitdomains.contracts.{RegistryState, defaultRegistryMap}
import org.ergoplatform.appkit.scalaapi.ErgoValueBuilder
import org.ergoplatform.appkit.{BlockchainContext, OutBox}
import scorex.crypto.hash
import sigmastate.eval.SigmaDsl

case class ConfigBoxBuilder(implicit ctx: BlockchainContext)
    extends BoxBuilder(configScript, configNft) {
  private var tldState: RegistryState = defaultRegistryMap
  private var reservedResolverHash = reservedResolverScriptHash
  private var resolverHash = resolverScriptHash

  def withTldState(v: RegistryState): this.type = {
    this.tldState = v
    this
  }

  def withReservedResolverHash(v: Array[Byte]): this.type = {
    this.reservedResolverHash = hash.Digest32 @@ v
    this
  }

  def withResolverHash(v: Array[Byte]): this.type = {
    this.resolverHash = hash.Digest32 @@ v
    this
  }

  override def build(): OutBox = {
    val scriptHashes = Array(reservedResolverHash, resolverHash)
    val ergoColls = SigmaDsl.Colls.fromArray(
      scriptHashes.map(SigmaDsl.Colls.fromArray(_))
    )
    val ergoValueScriptHashes = ErgoValueBuilder.buildFor(ergoColls)

    this
      .partialBuild()
      .registers(tldState.ergoValue, ergoValueScriptHashes)
      .build()
  }
}

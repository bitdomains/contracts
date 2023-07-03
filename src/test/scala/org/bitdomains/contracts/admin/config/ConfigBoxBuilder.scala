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
import org.ergoplatform.appkit.{BlockchainContext, ErgoValue, OutBox}
import scorex.crypto.hash
import sigmastate.eval.{Colls, SigmaDsl}
import special.collection.Coll

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
    val reserveResolverHashes = Array(reservedResolverHash)
    val resolverHashes = Array(resolverHash)
    val resolverArray = Array(
      SigmaDsl.Colls.fromArray(
        reserveResolverHashes.map(SigmaDsl.Colls.fromArray(_))
      ),
      SigmaDsl.Colls.fromArray(
        resolverHashes.map(SigmaDsl.Colls.fromArray(_))
      )
    )
    val scriptColls = SigmaDsl.Colls.fromArray(resolverArray)
    val ergoValueScriptHashes = ErgoValueBuilder.buildFor(scriptColls)

    this
      .partialBuild()
      .registers(tldState.ergoValue, ergoValueScriptHashes)
      .build()
  }
}

package org.bitdomains.contracts.registry.mintresolver

import bitdomains.Constants.{mintResolverNft, mintResolverScript}
import org.bitdomains.contracts.utils.builders.BoxBuilder
import org.ergoplatform.appkit.BlockchainContext

case class MintResolverBoxBuilder(implicit ctx: BlockchainContext)
    extends BoxBuilder(mintResolverScript, mintResolverNft) {}

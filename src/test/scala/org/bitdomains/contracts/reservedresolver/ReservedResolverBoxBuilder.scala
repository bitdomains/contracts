package org.bitdomains.contracts.reservedresolver

import bitdomains.Constants.reservedResolverScript
import org.bitdomains.contracts.utils.builders.BoxBuilder
import org.ergoplatform.appkit.BlockchainContext

case class ReservedResolverBoxBuilder(implicit ctx: BlockchainContext)
    extends BoxBuilder(reservedResolverScript) {}

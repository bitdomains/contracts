package org.bitdomains.contracts.registry.resolverreservation

import bitdomains.Constants.{resolverReservationNft, resolverReservationScript}
import org.bitdomains.contracts.utils.builders.BoxBuilder
import org.ergoplatform.appkit.BlockchainContext

case class ResolverReservationBoxBuilder(implicit ctx: BlockchainContext)
    extends BoxBuilder(resolverReservationScript, resolverReservationNft) {}

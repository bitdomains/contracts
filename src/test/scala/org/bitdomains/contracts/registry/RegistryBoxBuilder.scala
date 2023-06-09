package org.bitdomains.contracts.registry

import bitdomains.Constants.{registryNft, registryScript}
import io.getblok.getblok_plasma.ByteConversion.{
  convertsArrBytes,
  convertsString
}
import io.getblok.getblok_plasma.PlasmaParameters
import io.getblok.getblok_plasma.collections.PlasmaMap
import org.bitdomains.contracts.RegistryState
import org.bitdomains.contracts.utils.builders.BoxBuilder
import org.ergoplatform.appkit._
import sigmastate.AvlTreeFlags

case class RegistryBoxBuilder(implicit ctx: BlockchainContext)
    extends BoxBuilder(registryScript, registryNft) {
  private var resolversMap: RegistryState =
    new PlasmaMap(AvlTreeFlags.AllOperationsAllowed, PlasmaParameters.default)
  private var reservationsMap: RegistryState =
    new PlasmaMap(AvlTreeFlags.AllOperationsAllowed, PlasmaParameters.default)

  def withResolversMap(map: RegistryState): RegistryBoxBuilder = {
    this.resolversMap = map
    this
  }

  def withReservationsMap(map: RegistryState): RegistryBoxBuilder = {
    this.reservationsMap = map
    this
  }

  override def build(): OutBox = {
    this
      .partialBuild()
      .registers(
        this.resolversMap.ergoValue,
        this.reservationsMap.ergoValue
      )
      .build()
  }
}

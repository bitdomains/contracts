package org.bitdomains

import io.getblok.getblok_plasma.PlasmaParameters
import io.getblok.getblok_plasma.collections.PlasmaMap
import sigmastate.AvlTreeFlags

package object contracts {
  type RegistryState = PlasmaMap[Array[Byte], String]

  val defaultScript = "sigmaProp(true)"

  def defaultRegistryMap: RegistryState = {
    new PlasmaMap[Array[Byte], String](
      AvlTreeFlags.AllOperationsAllowed,
      PlasmaParameters.default
    )
  }
}

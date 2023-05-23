package org.bitdomains

import io.getblok.getblok_plasma.PlasmaParameters
import io.getblok.getblok_plasma.collections.PlasmaMap
import org.ergoplatform.appkit.{ErgoClient, NetworkType, RestApiErgoClient}
import sigmastate.AvlTreeFlags

package object contracts {
  type RegistryState = PlasmaMap[Array[Byte], String]

  val fakeIndex: Short = 1
  val fakeTxId1 =
    "f9e5ce5aa0d95f5d54a7bc89c46730d9662397067250aa18a0039631c0f5b809"
  val fakeTxId3 =
    "f9e5ce5aa0d95f5d54a7bc89c46730d9662397067250aa18a0039631c0f5b807"
  val defaultScript = "sigmaProp(true)"

  def defaultRegistryMap: RegistryState = {
    new PlasmaMap[Array[Byte], String](
      AvlTreeFlags.AllOperationsAllowed,
      PlasmaParameters.default
    )
  }

  def bytesToHex(bytes: Array[Byte]): String = {
    bytes.map("%02x".format(_)).mkString
  }

  def ergoClient: ErgoClient = {
    RestApiErgoClient.create(
      "http://168.138.185.215:9052/",
      NetworkType.TESTNET,
      "",
      ""
    )
  }
}

package org.bitdomains

import io.getblok.getblok_plasma.PlasmaParameters
import io.getblok.getblok_plasma.collections.PlasmaMap
import org.bouncycastle.util.BigIntegers
import org.ergoplatform.appkit.{
  ErgoClient,
  JavaHelpers,
  NetworkType,
  RestApiErgoClient,
  SecretString
}
import org.ergoplatform.wallet.secrets.ExtendedSecretKey
import scorex.utils.Random
import sigmastate.AvlTreeFlags
import sigmastate.basics.DLogProtocol.DLogProverInput

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

  def randomErgoId: String = {
    bytesToHex(Random.randomBytes(32))
  }

  def bytesToHex(bytes: Array[Byte]): String = {
    bytes.map("%02x".format(_)).mkString
  }

  def hexToBytes(hexString: String): Array[Byte] = {
    hexString.sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte)
  }

  val ergoClient: ErgoClient = {
    RestApiErgoClient.create(
      "https://ergo-node-testnet-weu.zoomout.io",
      NetworkType.TESTNET,
      "",
      ""
    )
  }

  def walletSk: ExtendedSecretKey = {
    val mnemonic = SecretString.create("not real mnemonic")
    val rootSecret =
      JavaHelpers.seedToMasterKey(mnemonic, SecretString.empty(), true)
    val path = JavaHelpers.eip3DerivationParent

    rootSecret.derive(path)
  }

  def randomProverInput: DLogProverInput = {
    DLogProverInput(BigIntegers.fromUnsignedByteArray(Random.randomBytes(32)))
  }
}

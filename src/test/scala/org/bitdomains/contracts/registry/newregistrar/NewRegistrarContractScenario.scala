package org.bitdomains.contracts.registry.newregistrar

import org.bitdomains.contracts._
import org.bitdomains.contracts.registry.RegistryBoxBuilder
import org.bitdomains.contracts.scenarios.ContractScenario
import org.ergoplatform.appkit.{BlockchainContext, ContextVar, InputBox, OutBox}
import scorex.crypto.hash.Blake2b256

case class NewRegistrarContractScenario(
    registrarsMap: RegistryState = defaultRegistryMap,
    tldToMint: String = "erg"
)(implicit
    ctx: BlockchainContext
) extends ContractScenario[NewRegistrarTransactionBuilder] {
  var registryIn: InputBox = RegistryBoxBuilder()
    .withRegistrarsMap(registrarsMap)
    .withValue(200000000000000000L)
    .build()
    .convertToInputWith(fakeTxId3, fakeIndex)

  var newRegistrarIn: InputBox = NewRegistrarBoxBuilder()
    .withTld(tldToMint)
    .build()
    .convertToInputWith(fakeTxId3, fakeIndex)

  var registryOut: OutBox =
    RegistryBoxBuilder().withRegistrarsMap(registrarsMap).build()

  // default value, should be updated during `insertTld` to get new tld value in output box
  var newRegistrarOut: OutBox = NewRegistrarBoxBuilder().build()

  def insertTld(tld: String = tldToMint, value: String = "01"): Unit = {
    val hashedTld = Blake2b256.hash(tld)
    val updatedRegistrarsMapResult = registrarsMap.insert((hashedTld, value))

    newRegistrarIn = newRegistrarIn.withContextVars(
      new ContextVar(0.toByte, updatedRegistrarsMapResult.proof.ergoValue)
    )
    registryOut = RegistryBoxBuilder()
      .withRegistrarsMap(registrarsMap)
      .build() // build outbox with registrar tld inserted
  }

  override def txBuilder: NewRegistrarTransactionBuilder = {
    NewRegistrarTransactionBuilder()
      .withRegistryIn(registryIn)
      .withNewRegistrarIn(newRegistrarIn)
      .withRegistryOut(registryOut)
      .withNewRegistrarOut(newRegistrarOut)
  }
}

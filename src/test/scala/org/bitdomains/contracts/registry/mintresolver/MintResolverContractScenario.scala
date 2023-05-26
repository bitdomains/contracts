package org.bitdomains.contracts.registry.mintresolver

import org.bitdomains.contracts.admin.config.ConfigBoxBuilder
import org.bitdomains.contracts.mintresolverrequest.MintResolverRequestBoxBuilder
import org.bitdomains.contracts.{
  RegistryState,
  bytesToHex,
  defaultRegistryMap,
  fakeIndex,
  fakeTxId1,
  fakeTxId3
}
import org.bitdomains.contracts.registry.RegistryBoxBuilder
import org.bitdomains.contracts.reservedresolver.ReservedResolverBoxBuilder
import org.bitdomains.contracts.resolver.ResolverBoxBuilder
import org.bitdomains.contracts.utils.scenarios.ContractScenario
import org.ergoplatform.appkit.{BlockchainContext, ContextVar}
import scorex.crypto.hash.Blake2b256

case class MintResolverContractScenario(implicit
    ctx: BlockchainContext
) extends ContractScenario[MintResolverTransactionBuilder] {
  var registrarsMap: RegistryState = {
    val defaultTld = "erg"
    val map = defaultRegistryMap
    map.insert((Blake2b256(defaultTld), bytesToHex(defaultTld.getBytes)))
    map
  }
  var resolversMap: RegistryState = defaultRegistryMap
  var reservationsMap: RegistryState = defaultRegistryMap

  var registryIn: RegistryBoxBuilder =
    RegistryBoxBuilder()
      .withValue(200000000000000000L)
      .withReservationsMap(reservationsMap)

  var registryOut: RegistryBoxBuilder =
    RegistryBoxBuilder()

  var mintResolverIn: MintResolverBoxBuilder = MintResolverBoxBuilder()
  var mintResolverContextVars: Seq[ContextVar] = Seq()

  var mintResolverOut: MintResolverBoxBuilder = MintResolverBoxBuilder()

  var mintResolverRequestIn: MintResolverRequestBoxBuilder =
    MintResolverRequestBoxBuilder()

  var reservedResolverIn: ReservedResolverBoxBuilder =
    ReservedResolverBoxBuilder()

  var resolverOut: ResolverBoxBuilder = ResolverBoxBuilder()

  var configDataIn: ConfigBoxBuilder = ConfigBoxBuilder()

  override def txBuilder: MintResolverTransactionBuilder = {
    val registryInBox =
      registryIn
        .withReservationsMap(reservationsMap)
        .build()
        .convertToInputWith(fakeTxId3, fakeIndex)

    // probably avl ops here

    val registryOutBox = registryOut
      .withReservationsMap(reservationsMap)
      .withResolversMap(resolversMap)
      .build()

    val mintResolverInBox = mintResolverIn
      .build()
      .convertToInputWith(fakeTxId1, fakeIndex)
      .withContextVars(mintResolverContextVars: _*)

    val mintResolverOutBox = mintResolverOut.build()

    val mintResolverRequestInBox =
      mintResolverRequestIn.build().convertToInputWith(fakeTxId1, fakeIndex)

    val reservedResolverInBox =
      reservedResolverIn.build().convertToInputWith(fakeTxId1, fakeIndex)

    val resolverOutBox = resolverOut.build()

    val configDataInBox =
      configDataIn
        .withTldState(registrarsMap)
        .build()
        .convertToInputWith(fakeTxId1, fakeIndex)

    MintResolverTransactionBuilder()
      .withRegistryIn(registryInBox)
      .withMintResolverIn(mintResolverInBox)
      .withMintResolverRequestIn(mintResolverRequestInBox)
      .withReservedResolverIn(reservedResolverInBox)
      .withRegistryOut(registryOutBox)
      .withMintResolverOut(mintResolverOutBox)
      .withResolverOut(resolverOutBox)
      .withConfigDataIn(configDataInBox)
  }
}

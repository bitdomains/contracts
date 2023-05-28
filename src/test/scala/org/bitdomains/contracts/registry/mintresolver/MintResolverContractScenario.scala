package org.bitdomains.contracts.registry.mintresolver

import org.bitdomains.contracts.admin.config.ConfigBoxBuilder
import org.bitdomains.contracts.mintresolverrequest.MintResolverRequestBoxBuilder
import org.bitdomains.contracts.{
  RegistryState,
  bytesToHex,
  defaultRegistryMap,
  fakeIndex,
  fakeTxId1,
  fakeTxId3,
  randomErgoId
}
import org.bitdomains.contracts.registry.RegistryBoxBuilder
import org.bitdomains.contracts.reservedresolver.ReservedResolverBoxBuilder
import org.bitdomains.contracts.resolver.ResolverBoxBuilder
import org.bitdomains.contracts.utils.scenarios.ContractScenario
import org.ergoplatform.appkit.{BlockchainContext, ContextVar}
import scorex.crypto.hash.Blake2b256

case class MintResolverContractScenario(
    existingReservedLabel: String = "aaaa",
    existingTld: String = "erg",
    overrideExistingTldState: Option[String] = None,
    existingReservationNft: String = randomErgoId
)(implicit
    ctx: BlockchainContext
) extends ContractScenario[MintResolverTransactionBuilder] {
  val mintLabel = existingReservedLabel
  val mintTld = existingTld

  var newResolverNft = ""
  var overrideNewResolverNft: Option[String] = None

  var tldState: RegistryState = {
    val map = defaultRegistryMap
    val tld = overrideExistingTldState.getOrElse(existingTld)
    map.insert((Blake2b256(tld), bytesToHex(tld.getBytes)))
    map
  }
  var reservationsMap: RegistryState = {
    val map = defaultRegistryMap
    map.insert(
      (Blake2b256(existingReservedLabel ++ existingTld), existingReservationNft)
    )
    map
  }
  var resolversMap: RegistryState = defaultRegistryMap

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
      .withTld(mintTld)
      .withLabel(mintLabel)

  var reservedResolverIn: ReservedResolverBoxBuilder =
    ReservedResolverBoxBuilder()

  var resolverOut: ResolverBoxBuilder = ResolverBoxBuilder()
    .withTld(existingTld)
    .withLabel(existingReservedLabel)
  var resolverOutNftAmount: Int = 1

  var configDataIn: ConfigBoxBuilder = ConfigBoxBuilder()

  def avlOps(): Unit = {
    val tldLookup = tldState.lookUp(Blake2b256(mintTld))
    val keyHash = Blake2b256(mintLabel ++ mintTld)
    val reservationRemove =
      reservationsMap.delete(keyHash)
    val resolversInsert = resolversMap.insert((keyHash, newResolverNft))

    mintResolverContextVars = mintResolverContextVars ++ Seq(
      new ContextVar(0.toByte, tldLookup.proof.ergoValue),
      new ContextVar(1.toByte, resolversInsert.proof.ergoValue),
      // this proof is also used for a AvlTree.get call in contract
      new ContextVar(2.toByte, reservationRemove.proof.ergoValue)
    )
  }

  override def txBuilder: MintResolverTransactionBuilder = {
    val registryInBox =
      registryIn
        .withReservationsMap(reservationsMap)
        .build()
        .convertToInputWith(fakeTxId3, fakeIndex)
    newResolverNft =
      overrideNewResolverNft.getOrElse(registryInBox.getId.toString)

    avlOps()

    val mintResolverInBox = mintResolverIn
      .build()
      .convertToInputWith(fakeTxId1, fakeIndex)
      .withContextVars(mintResolverContextVars: _*)

    val registryOutBox = registryOut
      .withReservationsMap(reservationsMap)
      .withResolversMap(resolversMap)
      .build()

    val mintResolverOutBox = mintResolverOut.build()

    val mintResolverRequestInBox =
      mintResolverRequestIn
        .withReservedResolverNftId(existingReservationNft)
        .build()
        .convertToInputWith(fakeTxId1, fakeIndex)

    val reservedResolverInBox =
      reservedResolverIn
        .withNftId(existingReservationNft)
        .withHashedReservation(Blake2b256(mintLabel ++ mintTld))
        .build()
        .convertToInputWith(fakeTxId1, fakeIndex)

    val resolverOutBox =
      resolverOut.withNftId(newResolverNft, resolverOutNftAmount).build()

    val configDataInBox =
      configDataIn
        .withTldState(tldState)
        .build()
        .convertToInputWith(fakeTxId1, fakeIndex)

    MintResolverTransactionBuilder()
      // inputs
      .withRegistryIn(registryInBox)
      .withMintResolverIn(mintResolverInBox)
      .withMintResolverRequestIn(mintResolverRequestInBox)
      .withReservedResolverIn(reservedResolverInBox)
      // outputs
      .withRegistryOut(registryOutBox)
      .withMintResolverOut(mintResolverOutBox)
      .withResolverOut(resolverOutBox)
      // data inputs
      .withConfigDataIn(configDataInBox)
  }
}

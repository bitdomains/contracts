package org.bitdomains.contracts.registry.resolverreservation

import org.bitdomains.contracts.{
  RegistryState,
  defaultRegistryMap,
  fakeIndex,
  fakeTxId3,
  randomErgoId
}
import org.bitdomains.contracts.registry.RegistryBoxBuilder
import org.bitdomains.contracts.reservedresolver.ReservedResolverBoxBuilder
import org.bitdomains.contracts.utils.scenarios.ContractScenario
import org.bitdomains.contracts.reserveresolverrequest.ReserveResolverRequestBoxBuilder
import org.ergoplatform.appkit.{BlockchainContext, ContextVar, InputBox, OutBox}
import scorex.crypto.hash.Blake2b256

case class ResolverReservationContractScenario(
    label: String = "myname",
    tld: String = "erg",
    resolversMap: RegistryState = defaultRegistryMap,
    reservationsMap: RegistryState = defaultRegistryMap
)(implicit
    ctx: BlockchainContext
) extends ContractScenario[ResolverReservationTransactionBuilder] {
  val reservedResolverNft: String = randomErgoId

  val hashedResolver: Array[Byte] = Blake2b256(label ++ tld)

  val registrarsMap: RegistryState = {
    val map = defaultRegistryMap
    map.insert((Blake2b256(tld), "01"))
    map
  }

  var registryIn: InputBox =
    RegistryBoxBuilder()
      .withValue(200000000000000000L)
      .withRegistrarsMap(registrarsMap)
      .build()
      .convertToInputWith(fakeTxId3, fakeIndex)

  var registryOut: OutBox =
    RegistryBoxBuilder().withRegistrarsMap(registrarsMap).build()

  var resolverReservationIn: InputBox = ResolverReservationBoxBuilder()
    .build()
    .convertToInputWith(fakeTxId3, fakeIndex)

  var resolverReservationOut: OutBox = ResolverReservationBoxBuilder().build()

  var reserveResolverRequestIn: InputBox =
    ReserveResolverRequestBoxBuilder()
      .withHashedReservation(hashedResolver)
      .build()
      .convertToInputWith(fakeTxId3, fakeIndex)

  var reservedResolverOut: OutBox = ReservedResolverBoxBuilder().build()

  def isNewResolverAvlOp(
      hashedResolver: Array[Byte] = this.hashedResolver
  ): Unit = {
    val opResult = resolversMap.lookUp(hashedResolver)

    resolverReservationIn = resolverReservationIn.withContextVars(
      new ContextVar(0.toByte, opResult.proof.ergoValue)
    )
  }

  def insertReservationAvlOp(
      hashedResolver: Array[Byte] = this.hashedResolver
  ): Unit = {
    val opResult = reservationsMap.insert((hashedResolver, reservedResolverNft))

    resolverReservationIn = resolverReservationIn.withContextVars(
      new ContextVar(1.toByte, opResult.proof.ergoValue)
    )
    registryOut = RegistryBoxBuilder()
      .withReservationsMap(reservationsMap)
      .build()
  }

  override def txBuilder: ResolverReservationTransactionBuilder = {
    ResolverReservationTransactionBuilder()
      .withRegistryIn(registryIn)
      .withRegistryOut(registryOut)
      .withResolverReservationIn(resolverReservationIn)
      .withResolverReservationOut(resolverReservationOut)
      .withReserveResolverRequestIn(reserveResolverRequestIn)
      .withReservedResolverOut(reservedResolverOut)
  }
}

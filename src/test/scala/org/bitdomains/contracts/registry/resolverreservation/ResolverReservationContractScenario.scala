package org.bitdomains.contracts.registry.resolverreservation

import org.bitdomains.contracts.{
  RegistryState,
  defaultRegistryMap,
  fakeIndex,
  fakeTxId3
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
  def expectedReservedResolverNft: String = {
    registryIn.getId.toString
  }

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
      .withReservationsMap(reservationsMap)
      .build()
      .convertToInputWith(fakeTxId3, fakeIndex)

  var registryOut: OutBox =
    RegistryBoxBuilder()
      .withRegistrarsMap(registrarsMap)
      .build()

  var resolverReservationIn: InputBox = ResolverReservationBoxBuilder()
    .build()
    .convertToInputWith(fakeTxId3, fakeIndex)

  var resolverReservationOut: OutBox = ResolverReservationBoxBuilder().build()

  var reserveResolverRequestIn: InputBox =
    ReserveResolverRequestBoxBuilder()
      .withHashedReservation(hashedResolver)
      .build()
      .convertToInputWith(fakeTxId3, fakeIndex)

  var reservedResolverOut: OutBox =
    ReservedResolverBoxBuilder().withNftId(expectedReservedResolverNft).build()

  def doAvlOps(
      hashedResolver: Array[Byte] = this.hashedResolver,
      insertedResolverNft: String = this.expectedReservedResolverNft
  ): Unit = {
    val lookupOp = resolversMap.lookUp(hashedResolver)
    val insertOp = reservationsMap.insert(
      (hashedResolver, insertedResolverNft)
    )

    resolverReservationIn = resolverReservationIn.withContextVars(
      new ContextVar(0.toByte, lookupOp.proof.ergoValue),
      new ContextVar(1.toByte, insertOp.proof.ergoValue)
    )
    registryOut = RegistryBoxBuilder()
      .withRegistrarsMap(registrarsMap)
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

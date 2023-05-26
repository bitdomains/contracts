package org.bitdomains.contracts.registry.resolverreservation

import org.bitdomains.contracts.registry.RegistryBoxBuilder
import org.bitdomains.contracts.reservedresolver.ReservedResolverBoxBuilder
import org.bitdomains.contracts.reserveresolverrequest.ReserveResolverRequestBoxBuilder
import org.bitdomains.contracts.utils.scenarios.ContractScenario
import org.bitdomains.contracts._
import org.ergoplatform.appkit.{BlockchainContext, ContextVar}
import scorex.crypto.hash.Blake2b256

case class ResolverReservationContractScenario(
    requestLabel: String = "myname",
    tld: String = "erg",
    resolversMap: RegistryState = defaultRegistryMap,
    reservationsMap: RegistryState = defaultRegistryMap
)(implicit
    ctx: BlockchainContext
) extends ContractScenario[ResolverReservationTransactionBuilder] {
  val hashedResolver: Array[Byte] = Blake2b256(requestLabel ++ tld)

  var registryIn: RegistryBoxBuilder =
    RegistryBoxBuilder()
      .withValue(200000000000000000L)
      .withReservationsMap(reservationsMap)
  var resolverReservationInVars: Seq[ContextVar] = Seq()

  var registryOut: RegistryBoxBuilder =
    RegistryBoxBuilder()

  var resolverReservationIn: ResolverReservationBoxBuilder =
    ResolverReservationBoxBuilder()

  var resolverReservationOut: ResolverReservationBoxBuilder =
    ResolverReservationBoxBuilder()

  var reserveResolverRequestIn: ReserveResolverRequestBoxBuilder =
    ReserveResolverRequestBoxBuilder()
      .withHashedReservation(hashedResolver)

  var reservedResolverOut: ReservedResolverBoxBuilder =
    ReservedResolverBoxBuilder()
  // optional nft override, defaults to expected nft (registryBox.id)
  var reservedResolverOutNftOverride: Option[String] = None
  var reservedResolverOutNftAmount: Int = 1

  def doAvlOps(
      hashedResolver: Array[Byte] = this.hashedResolver,
      insertedResolverNft: String = ""
  ): Unit = {
    val lookupOp = resolversMap.lookUp(hashedResolver)
    val insertOp = reservationsMap.insert(
      (hashedResolver, insertedResolverNft)
    )

    resolverReservationInVars = resolverReservationInVars ++ Seq(
      new ContextVar(0.toByte, lookupOp.proof.ergoValue),
      new ContextVar(1.toByte, insertOp.proof.ergoValue)
    )
  }

  override def txBuilder: ResolverReservationTransactionBuilder = {
    val registryInBox =
      registryIn
        .withReservationsMap(reservationsMap)
        .build()
        .convertToInputWith(fakeTxId3, fakeIndex)

    doAvlOps(insertedResolverNft = registryInBox.getId.toString)

    registryOut = registryOut.withReservationsMap(reservationsMap)
    val outNftId =
      reservedResolverOutNftOverride.getOrElse(registryInBox.getId.toString)
    reservedResolverOut = reservedResolverOut.withNftId(
      outNftId,
      reservedResolverOutNftAmount
    )

    val resolverReservationInBox = resolverReservationIn
      .build()
      .convertToInputWith(fakeTxId1, fakeIndex)
      .withContextVars(resolverReservationInVars: _*)

    ResolverReservationTransactionBuilder()
      .withRegistryIn(registryInBox)
      .withRegistryOut(registryOut.build())
      .withResolverReservationIn(resolverReservationInBox)
      .withResolverReservationOut(resolverReservationOut.build())
      .withReserveResolverRequestIn(
        reserveResolverRequestIn
          .build()
          .convertToInputWith(fakeTxId1, fakeIndex)
      )
      .withReservedResolverOut(reservedResolverOut.build())
  }
}

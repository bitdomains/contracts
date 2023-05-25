package org.bitdomains.contracts.registry.resolverreservation

import org.bitdomains.contracts.utils.builders.TransactionBuilder
import org.ergoplatform.appkit.{
  BlockchainContext,
  InputBox,
  OutBox,
  UnsignedTransaction
}

case class ResolverReservationTransactionBuilder(implicit
    ctx: BlockchainContext
) extends TransactionBuilder {
  private var registryIn: Option[InputBox] = None
  private var registryOut: Option[OutBox] = None
  private var resolverReservationIn: Option[InputBox] = None
  private var resolverReservationOut: Option[OutBox] = None
  private var reserveResolverRequestIn: Option[InputBox] = None
  private var reservedResolverOut: Option[OutBox] = None
  private var configDataIn: Option[InputBox] = None

  def withRegistryIn(box: InputBox): this.type = {
    this.registryIn = Some(box)
    this
  }

  def withRegistryOut(box: OutBox): this.type = {
    this.registryOut = Some(box)
    this
  }

  def withResolverReservationIn(box: InputBox): this.type = {
    this.resolverReservationIn = Some(box)
    this
  }

  def withResolverReservationOut(box: OutBox): this.type = {
    this.resolverReservationOut = Some(box)
    this
  }

  def withReserveResolverRequestIn(box: InputBox): this.type = {
    this.reserveResolverRequestIn = Some(box)
    this
  }

  def withReservedResolverOut(box: OutBox): this.type = {
    this.reservedResolverOut = Some(box)
    this
  }

  def withConfigDataIn(box: InputBox): this.type = {
    this.configDataIn = Some(box)
    this
  }

  override def build(): UnsignedTransaction = {
    this
      .partialBuild()
      .addInputs(
        Seq(
          registryIn,
          resolverReservationIn,
          reserveResolverRequestIn
        ).flatten: _*
      )
      .addOutputs(
        Seq(
          registryOut,
          resolverReservationOut,
          reservedResolverOut
        ).flatten: _*
      )
      .addDataInputs(Seq(configDataIn).flatten: _*)
      .build()
  }
}

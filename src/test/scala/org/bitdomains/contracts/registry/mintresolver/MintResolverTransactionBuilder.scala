package org.bitdomains.contracts.registry.mintresolver

import org.bitdomains.contracts.utils.builders.TransactionBuilder
import org.ergoplatform.appkit.{
  BlockchainContext,
  InputBox,
  OutBox,
  UnsignedTransaction
}

case class MintResolverTransactionBuilder(implicit
    ctx: BlockchainContext
) extends TransactionBuilder {
  private var registryIn: Option[InputBox] = None
  private var registryOut: Option[OutBox] = None
  private var mintResolverIn: Option[InputBox] = None
  private var mintResolverOut: Option[OutBox] = None
  private var mintResolverRequestIn: Option[InputBox] = None
  private var reservedResolverIn: Option[InputBox] = None
  private var resolverOut: Option[OutBox] = None
  private var configDataIn: Option[InputBox] = None

  def withRegistryIn(box: InputBox): this.type = {
    this.registryIn = Some(box)
    this
  }

  def withRegistryOut(box: OutBox): this.type = {
    this.registryOut = Some(box)
    this
  }

  def withMintResolverIn(box: InputBox): this.type = {
    this.mintResolverIn = Some(box)
    this
  }

  def withMintResolverOut(box: OutBox): this.type = {
    this.mintResolverOut = Some(box)
    this
  }

  def withMintResolverRequestIn(box: InputBox): this.type = {
    this.mintResolverRequestIn = Some(box)
    this
  }

  def withReservedResolverIn(box: InputBox): this.type = {
    this.reservedResolverIn = Some(box)
    this
  }

  def withResolverOut(box: OutBox): this.type = {
    this.resolverOut = Some(box)
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
          mintResolverIn,
          mintResolverRequestIn,
          reservedResolverIn
        ).flatten: _*
      )
      .addOutputs(
        Seq(
          registryOut,
          mintResolverOut,
          resolverOut
        ).flatten: _*
      )
      .addDataInputs(Seq(configDataIn).flatten: _*)
      .build()
  }
}

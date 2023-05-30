package org.bitdomains.contracts.resolver

import org.bitdomains.contracts.utils.builders.TransactionBuilder
import org.ergoplatform.appkit.{
  BlockchainContext,
  InputBox,
  OutBox,
  UnsignedTransaction
}

case class ResolverTransactionBuilder(implicit
    ctx: BlockchainContext
) extends TransactionBuilder {
  private var resolverIn: Option[InputBox] = None
  private var resolverOut: Option[OutBox] = None

  def withResolverIn(box: InputBox): this.type = {
    this.resolverIn = Some(box)
    this
  }

  def withResolverOut(box: OutBox): this.type = {
    this.resolverOut = Some(box)
    this
  }

  def build(): UnsignedTransaction = {
    this
      .partialBuild()
      .addInputs(
        Seq(
          resolverIn
        ).flatten: _*
      )
      .addOutputs(
        Seq(
          resolverOut
        ).flatten: _*
      )
      .build()
  }
}

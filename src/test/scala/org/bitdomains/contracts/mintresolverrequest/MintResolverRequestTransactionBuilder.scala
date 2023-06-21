package org.bitdomains.contracts.mintresolverrequest

import org.bitdomains.contracts.utils.builders.TransactionBuilder
import org.ergoplatform.appkit.{
  BlockchainContext,
  InputBox,
  OutBox,
  UnsignedTransaction
}

case class MintResolverRequestTransactionBuilder(implicit
    ctx: BlockchainContext
) extends TransactionBuilder {
  private var mintResolverRequestIn: Option[InputBox] = None

  private var refundOut: Option[OutBox] = None

  def withMintResolverRequestIn(box: InputBox): this.type = {
    mintResolverRequestIn = Some(box)
    this
  }

  def withRefundOut(box: OutBox): this.type = {
    refundOut = Some(box)
    this
  }

  override def build(): UnsignedTransaction = {
    this
      .partialBuild()
      .addInputs(
        Seq(mintResolverRequestIn).flatten: _*
      )
      .addOutputs(
        Seq(refundOut).flatten: _*
      )
      .build()
  }
}

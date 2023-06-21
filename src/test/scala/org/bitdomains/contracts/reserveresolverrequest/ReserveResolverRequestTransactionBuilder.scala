package org.bitdomains.contracts.reserveresolverrequest

import org.bitdomains.contracts.utils.builders.TransactionBuilder
import org.ergoplatform.appkit.{
  BlockchainContext,
  InputBox,
  OutBox,
  UnsignedTransaction
}

case class ReserveResolverRequestTransactionBuilder(implicit
    ctx: BlockchainContext
) extends TransactionBuilder {
  private var reserveResolverRequestIn: Option[InputBox] = None

  private var refundOut: Option[OutBox] = None

  def withReserveResolverRequestIn(box: InputBox): this.type = {
    reserveResolverRequestIn = Some(box)
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
        Seq(reserveResolverRequestIn).flatten: _*
      )
      .addOutputs(
        Seq(refundOut).flatten: _*
      )
      .build()
  }
}

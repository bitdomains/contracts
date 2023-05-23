package org.bitdomains.contracts.builders

import org.ergoplatform.appkit.{
  Address,
  BlockchainContext,
  UnsignedTransaction,
  UnsignedTransactionBuilder
}

abstract class TransactionBuilder(
    defaultFee: Long = 1e7.toLong,
    defaultChangeAddress: Address = Address.create("4MQyML64GnzMxZgm")
)(implicit ctx: BlockchainContext)
    extends BaseBuilder[UnsignedTransactionBuilder, UnsignedTransaction] {
  var fee: Long = defaultFee
  var changeAddress: Address = defaultChangeAddress

  def withFee(fee: Long): this.type = {
    this.fee = fee
    this
  }

  def withChangeAddress(changeAddress: Address): this.type = {
    this.changeAddress = changeAddress
    this
  }

  override def partialBuild(): UnsignedTransactionBuilder = {
    tb.fee(this.fee).sendChangeTo(this.changeAddress)
  }
}
package org.bitdomains.contracts.utils.builders

import org.ergoplatform.appkit.{
  Address,
  BlockchainContext,
  Parameters,
  UnsignedTransaction,
  UnsignedTransactionBuilder
}

abstract class TransactionBuilder(
    defaultFee: Long = Parameters.MinFee,
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

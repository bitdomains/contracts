package org.bitdomains.contracts.admin.config

import org.bitdomains.contracts.utils.builders.TransactionBuilder
import org.ergoplatform.appkit.{
  BlockchainContext,
  InputBox,
  OutBox,
  UnsignedTransaction
}

case class ConfigTransactionBuilder(implicit ctx: BlockchainContext)
    extends TransactionBuilder {
  private var configIn: Option[InputBox] = None
  private var configOut: Option[OutBox] = None
  private var adminIn: Option[InputBox] = None
  private var adminOut: Option[OutBox] = None

  def withConfigIn(box: InputBox): this.type = {
    configIn = Some(box)
    this
  }

  def withConfigOut(box: OutBox): this.type = {
    configOut = Some(box)
    this
  }

  def withAdminIn(box: InputBox): this.type = {
    adminIn = Some(box)
    this
  }

  def withAdminOut(box: OutBox): this.type = {
    adminOut = Some(box)
    this
  }

  override def build(): UnsignedTransaction = {
    this
      .partialBuild()
      .addInputs(
        Seq(configIn, adminIn).flatten: _*
      )
      .addOutputs(
        Seq(configOut, adminOut).flatten: _*
      )
      .build()
  }
}

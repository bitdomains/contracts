package org.bitdomains.contracts.registry.newregistrar

import org.bitdomains.contracts.builders.TransactionBuilder
import org.bitdomains.contracts.registry.RegistryAdminBoxBuilder
import org.ergoplatform.appkit.{
  BlockchainContext,
  InputBox,
  OutBox,
  UnsignedTransaction
}

case class NewRegistrarTransactionBuilder(implicit ctx: BlockchainContext)
    extends TransactionBuilder {

  private var registryIn: Option[InputBox] = None
  private var registryOut: Option[OutBox] = None
  private var newRegistrarIn: Option[InputBox] = None
  private var newRegistrarOut: Option[OutBox] = None
  private var adminIn: Option[InputBox] = Some(
    RegistryAdminBoxBuilder()
      .build()
      .convertToInputWith(
        "94af8793a1f7b427831dcb48368ffc55c68d319d525ea24510ac38b75e280a8d",
        1
      )
  )
  private var adminOut: Option[OutBox] = Some(RegistryAdminBoxBuilder().build())

  def withRegistryIn(box: InputBox): this.type = {
    this.registryIn = Some(box)
    this
  }

  def withRegistryOut(box: OutBox): this.type = {
    this.registryOut = Some(box)
    this
  }

  def withNewRegistrarIn(box: InputBox): this.type = {
    this.newRegistrarIn = Some(box)
    this
  }

  def withNewRegistrarOut(box: OutBox): this.type = {
    this.newRegistrarOut = Some(box)
    this
  }

  def withAdminIn(box: InputBox): this.type = {
    this.adminIn = Some(box)
    this
  }

  def withAdminOut(box: OutBox): this.type = {
    this.adminOut = Some(box)
    this
  }

  override def build(): UnsignedTransaction = {
    this
      .partialBuild()
      .addInputs(
        Seq(this.registryIn, this.newRegistrarIn, this.adminIn).flatten: _*
      )
      .addOutputs(
        Seq(this.registryOut, this.newRegistrarOut, this.adminOut).flatten: _*
      )
      .build()
  }
}

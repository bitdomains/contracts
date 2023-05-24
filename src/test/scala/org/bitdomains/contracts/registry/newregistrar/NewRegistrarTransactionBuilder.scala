package org.bitdomains.contracts.registry.newregistrar

import org.bitdomains.contracts.registry.RegistryAdminBoxBuilder
import org.bitdomains.contracts.utils.builders.TransactionBuilder
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
    registryIn = Some(box)
    this
  }

  def withRegistryOut(box: OutBox): this.type = {
    registryOut = Some(box)
    this
  }

  def withNewRegistrarIn(box: InputBox): this.type = {
    newRegistrarIn = Some(box)
    this
  }

  def withNewRegistrarOut(box: OutBox): this.type = {
    newRegistrarOut = Some(box)
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
        Seq(registryIn, newRegistrarIn, adminIn).flatten: _*
      )
      .addOutputs(
        Seq(registryOut, newRegistrarOut, adminOut).flatten: _*
      )
      .build()
  }
}

package org.bitdomains.contracts.admin.config

import org.bitdomains.contracts._
import org.bitdomains.contracts.admin.AdminBoxBuilder
import org.bitdomains.contracts.utils.scenarios.ContractScenario
import org.ergoplatform.appkit.{BlockchainContext, ContextVar, ErgoValue}
import scorex.crypto.hash.Blake2b256

sealed trait ConfigAction {
  def id: Byte
}

case object UpdateRegistrarsConfigAction extends ConfigAction {
  override def id: Byte = 0
}

case object UpdateScriptHashesAction extends ConfigAction {
  override def id: Byte = 1
}

case object UpdatePricingAction extends ConfigAction {
  override def id: Byte = 2
}

case object UpdateFeesAction extends ConfigAction {
  override def id: Byte = 3
}

case class ConfigContractScenario(implicit
    ctx: BlockchainContext
) extends ContractScenario[ConfigTransactionBuilder] {
  var contextVars: Seq[ContextVar] = Seq()

  var action: ConfigAction = UpdateRegistrarsConfigAction

  var insertTld: String = "erg"
  var tldState: RegistryState = defaultRegistryMap

  var configIn: ConfigBoxBuilder =
    ConfigBoxBuilder()
      .withValue(500000000000000000L)
      .withTldState(tldState)

  var configOut: ConfigBoxBuilder =
    ConfigBoxBuilder()
      .withTldState(tldState)

  var adminIn: AdminBoxBuilder = AdminBoxBuilder()

  var adminOut: AdminBoxBuilder = AdminBoxBuilder()

  def updateTldAction(
      tld: String = insertTld
  ): Unit = {
    val hashedTld = Blake2b256(tld)
    val opResult = tldState.insert((hashedTld, bytesToHex(tld.getBytes)))

    contextVars = contextVars ++ Seq(
      new ContextVar(1.toByte, ErgoValue.of(tld.getBytes)),
      new ContextVar(2.toByte, opResult.proof.ergoValue)
    )
  }

  override def txBuilder: ConfigTransactionBuilder = {
    val adminInBox = adminIn
      .build()
      .convertToInputWith(
        "94af8793a1f7b427831dcb48368ffc55c68d319d525ea24510ac38b75e280a8d",
        1
      )
    val adminOutBox = adminOut.build()
    var configInBox = configIn.build().convertToInputWith(fakeTxId1, fakeIndex)

    contextVars =
      contextVars ++ Seq(new ContextVar(0.toByte, ErgoValue.of(action.id)))

    if (action == UpdateRegistrarsConfigAction) {
      updateTldAction()
    }

    val configOutBox = configOut.withTldState(tldState).build()
    configInBox = configInBox.withContextVars(contextVars: _*)

    ConfigTransactionBuilder()
      .withConfigIn(configInBox)
      .withConfigOut(configOutBox)
      .withAdminIn(adminInBox)
      .withAdminOut(adminOutBox)
  }
}

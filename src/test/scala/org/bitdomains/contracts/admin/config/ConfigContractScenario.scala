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

  var tldInsertProof: Array[Byte] = Array()

  def updateTldAction(): Unit = {
    val hashedTld = Blake2b256(insertTld)
    val opResult = tldState.insert((hashedTld, bytesToHex(insertTld.getBytes)))

    tldInsertProof = opResult.proof.bytes
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

    if (action == UpdateRegistrarsConfigAction) {
      updateTldAction()
    }

    contextVars = contextVars ++ Seq(
      new ContextVar(0.toByte, ErgoValue.of(action.id)),
      // all context vars should always be added regardless of which action is performed
      // this is because we can't control inlining of ValDef's in the ergo script evaluation
      // which means the context var could attempt to be accessed even if it's not used
      // If the context var is not present, the script will fail so provide placeholders
      // if the value is not actually used
      new ContextVar(1.toByte, ErgoValue.of(insertTld.getBytes)),
      new ContextVar(2.toByte, ErgoValue.of(tldInsertProof))
    )

    val configOutBox = configOut.withTldState(tldState).build()
    configInBox = configInBox.withContextVars(contextVars: _*)

    ConfigTransactionBuilder()
      .withConfigIn(configInBox)
      .withConfigOut(configOutBox)
      .withAdminIn(adminInBox)
      .withAdminOut(adminOutBox)
  }
}

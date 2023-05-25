package org.bitdomains.contracts.admin.config

import org.bitdomains.contracts._
import org.bitdomains.contracts.utils.scenarios.ContractScenario
import org.ergoplatform.appkit.{
  BlockchainContext,
  ContextVar,
  ErgoValue,
  InputBox,
  OutBox
}
import org.bitdomains.contracts.registry.RegistryAdminBoxBuilder
import scorex.crypto.hash.Blake2b256

sealed trait ConfigAction {
  def id: Byte
}

case object UpdateTldConfigAction extends ConfigAction {
  override def id: Byte = 1
}

case class ConfigContractScenario(implicit
    ctx: BlockchainContext
) extends ContractScenario[ConfigTransactionBuilder] {
  var action: ConfigAction = UpdateTldConfigAction

  var insertTld: String = "erg"
  var tldState: RegistryState = defaultRegistryMap

  var configIn: InputBox =
    ConfigBoxBuilder()
      .withValue(500000000000000000L)
      .withTldState(tldState)
      .build()
      .convertToInputWith(fakeTxId3, fakeIndex)

  var configOut: OutBox =
    ConfigBoxBuilder()
      .withTldState(tldState)
      .build()

  var adminIn: InputBox = RegistryAdminBoxBuilder()
    .build()
    .convertToInputWith(
      "94af8793a1f7b427831dcb48368ffc55c68d319d525ea24510ac38b75e280a8d",
      1
    )

  var adminOut: OutBox = RegistryAdminBoxBuilder()
    .build()

  def updateTldAction(
      tld: String = insertTld,
      valueOpt: Option[String] = None
  ): Unit = {
    val hashedTld = Blake2b256(tld)
    val opResult = tldState.insert((hashedTld, bytesToHex(tld.getBytes)))

    configIn = configIn.withContextVars(
      new ContextVar(0.toByte, ErgoValue.of(UpdateTldConfigAction.id)),
      new ContextVar(1.toByte, ErgoValue.of(tld.getBytes)),
      new ContextVar(2.toByte, opResult.proof.ergoValue)
    )
    configOut = ConfigBoxBuilder().withTldState(tldState).build()
  }

  override def txBuilder: ConfigTransactionBuilder = {
    ConfigTransactionBuilder()
      .withConfigIn(configIn)
      .withConfigOut(configOut)
      .withAdminIn(adminIn)
      .withAdminOut(adminOut)
  }
}

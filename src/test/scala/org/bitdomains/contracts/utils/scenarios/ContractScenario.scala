package org.bitdomains.contracts.utils.scenarios

import org.bitdomains.contracts.utils.builders.TransactionBuilder
import org.bitdomains.contracts.walletSk
import org.ergoplatform.appkit.{BlockchainContext, ErgoProver}

abstract class ContractScenario[T <: TransactionBuilder](implicit
    ctx: BlockchainContext
) {
  var prover: ErgoProver =
    ctx.newProverBuilder().withDLogSecret(walletSk.privateInput.w).build()

  def mkAndSignTx(): Unit = {
    prover.sign(txBuilder.build())
  }

  def beforeTxBuild(): Unit = {}

  def txBuilder: T
}

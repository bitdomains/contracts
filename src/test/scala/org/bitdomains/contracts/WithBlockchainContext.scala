package org.bitdomains.contracts

import org.ergoplatform.appkit.{BlockchainContext, ErgoClient}

trait WithBlockchainContext {
  def node: ErgoClient = ergoClient

  def withBlockchain(test: => BlockchainContext => Unit): Unit = {
    node.execute { implicit ctx: BlockchainContext =>
      test(ctx)
    }
  }
}

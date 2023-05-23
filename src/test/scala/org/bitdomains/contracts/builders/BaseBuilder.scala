package org.bitdomains.contracts.builders

import org.ergoplatform.appkit.{BlockchainContext, UnsignedTransactionBuilder}

abstract class BaseBuilder[A, B]()(implicit ctx: BlockchainContext) {
  protected val tb: UnsignedTransactionBuilder = ctx.newTxBuilder()

  def partialBuild(): A

  def build(): B
}

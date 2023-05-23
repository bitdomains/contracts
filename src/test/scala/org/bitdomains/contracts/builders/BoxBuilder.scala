package org.bitdomains.contracts.builders

import org.bitdomains.contracts.defaultScript
import org.ergoplatform.appkit.{
  BlockchainContext,
  ConstantsBuilder,
  ErgoToken,
  OutBox,
  OutBoxBuilder
}

abstract class BoxBuilder(
    defaultScript: String = defaultScript,
    defaultNftId: String = "",
    defaultValue: Long = 100000000000000000L
)(implicit ctx: BlockchainContext)
    extends BaseBuilder[OutBoxBuilder, OutBox] {
  var script: String = defaultScript
  var nftId: String = defaultNftId
  var value: Long = defaultValue

  def withScript(script: String): this.type = {
    this.script = script
    this
  }

  def withNftId(nftId: String): this.type = {
    this.nftId = nftId
    this
  }

  def withValue(value: Long): this.type = {
    this.value = value
    this
  }

  override def partialBuild(): OutBoxBuilder = {
    var builder = tb
      .outBoxBuilder()
      .value(this.value)
      .contract(ctx.compileContract(ConstantsBuilder.empty(), this.script))

    if (this.nftId != "") {
      builder = builder.tokens(new ErgoToken(this.nftId, 1))
    }

    builder
  }
}

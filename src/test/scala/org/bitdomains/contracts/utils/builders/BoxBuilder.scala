package org.bitdomains.contracts.utils.builders

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
  var nft: Option[ErgoToken] =
    if (defaultNftId != "") Some(new ErgoToken(defaultNftId, 1)) else { None }
  var value: Long = defaultValue
  var tokens: Seq[ErgoToken] = Seq()

  def withScript(script: String): this.type = {
    this.script = script
    this
  }

  def withNftId(nftId: String, amount: Int = 1): this.type = {
    this.nft = Some(new ErgoToken(nftId, amount))
    this
  }

  def withValue(value: Long): this.type = {
    this.value = value
    this
  }

  def withTokens(v: ErgoToken*): this.type = {
    tokens = tokens ++ v
    this
  }

  override def partialBuild(): OutBoxBuilder = {
    var builder = tb
      .outBoxBuilder()
      .value(this.value)
      .contract(ctx.compileContract(ConstantsBuilder.empty(), this.script))

    var allTokens: Seq[ErgoToken] = Seq()

    if (this.nft.nonEmpty) {
      allTokens = allTokens ++ Seq(nft.get)
    }

    allTokens = allTokens ++ tokens

    if (allTokens.nonEmpty) {
      builder = builder.tokens(allTokens: _*)
    }

    builder
  }

  override def build(): OutBox = {
    partialBuild().build()
  }
}

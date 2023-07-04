{
  // Mint resolver request script
  //
  // TRANSACTIONS
  //
  // [1] Mint Resolver
  // Creates a resolver box/nft that is used for address resolution.
  // This transaction is created on behalf of users by off-chain bots to create resolvers
  // for their specified label (name) and registrar (TLD) by chaining together many `MintResolver` transactions.
  //
  //   Input                      |  Output        |  Data-Input
  // ------------------------------------------------------------
  // 0 Registry                   |  Registry      |
  // 1 MintResolver               |  MintResolver  |
  // 2 MintResolverRequest        |  Resolver      |
  // 3 ReservedResolver           |                |
  //
  // [2] Refund
  // This box can be spent to recind the request if the spender can `proveDlog` the PK
  // contained in R5. I.e sign the transaction using the associated SK.
  //
  // REGISTERS
  //  R4: (Coll[Byte])    Reserved resolver box nft id. Provided so off-chain bots can find the reservation box
  //                        required for `Registry.MintResolver` txns.
  //  R5: (SigmaProp)     Sigma proposition of the buyer, used to refund the reservation to the buyer.
  //  R6: (Coll[Coll[Byte]])    Labels making up the domain name. I.e [myname, erg] == "myname.erg"
  //  R7: (Coll[Byte])    Address to resolve to, this should be set based on the TLD.
  //                        For example if TLD is "erg" an Ergo address, if TLD is "ada" a Cardano address.

  val isRefundTx = INPUTS.size == 1

  val validMintResolverTx = {
    val registryInBox = INPUTS(0)
    val mintResolverInBox = INPUTS(1)

    val isMintResolverTx = registryInBox.tokens(0)._1 == fromBase16("$registryNft") &&
      mintResolverInBox.tokens(0)._1 == fromBase16("$mintResolverNft")

    isMintResolverTx
  }

  val buyerProp = SELF.R5[SigmaProp].get

  if (isRefundTx) {
    buyerProp
  } else sigmaProp(validMintResolverTx)
}

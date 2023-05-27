{
  // Mint resolver request script
  //
  // TRANSACTIONS
  //
  // [1] Mint Resolver
  // Creates a resolver box/nft that is used for address resolution.
  // This action is called by users & on behalf of users by off-chain bots to create resolvers
  // for their specified label (name) and registrar (TLD).
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
  //  R5: (GroupElement)  PK of the buyer, transaction must be signed with the associated SK to ensure
  //                        the buyer can spend/use the Resolver.
  //  R6: (Coll[Byte])    Label (name) that is used to resolve an address.
  //  R7: (Coll[Byte])    Registrar/TLD, "erg" for example.
  //  R8: (Coll[Byte])    Address to resolve to, this should be set based on the TLD.
  //                        For example if TLD is "erg" an Ergo address, if TLD is "ada" a Cardano address.

  // indexes
  val registryIndex = 0
  val mintResolverIndex = 1

  // boxes
  val registryInBox = INPUTS(registryIndex)
  val mintResolverInBox = INPUTS(mintResolverIndex)

  // registers
  val buyerPk = SELF.R5[GroupElement].get

  // nfts
  val registryNft = fromBase16("$registryNft")
  val mintResolverNft = fromBase16("$mintResolverNft")

  // validation
  val isMintResolverTx = registryInBox.tokens(0)._1 == registryNft &&
    mintResolverInBox.tokens(0)._1 == mintResolverNft

  val isBuyer = proveDlog(buyerPk)

  isBuyer || sigmaProp(isMintResolverTx)
}
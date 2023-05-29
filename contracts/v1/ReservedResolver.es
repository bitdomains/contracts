{
  // Reserved resolver script
  //
  // This contract is used to reserve a resolver box/nft that is later required to mint a real resolver.
  // It is necessary to make a reservation before minting a resolver to prevent frontrunning.
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
  // 0 Registry                   |  Registry      | Config
  // 1 MintResolver               |  MintResolver  |
  // 2 MintResolverRequest        |  Resolver      |
  // 3 ReservedResolver           |                |
  //
  // REGISTERS
  //  R4: (Coll[Byte])    Hash of the reservation: blake2b256(label ++ tld).
  //  R5: (GroupElement)  PK of the buyer.
  //  R6: (Coll[Byte])    Address to resolve to, this should be set based on the TLD.
  //                        For example if TLD is "erg" an Ergo address, if TLD is "ada" a Cardano address.
  //
  // TOKENS
  //  0: (Coll[Byte]) Nft id uniquely identifying this reservation.

  // boxes
  val registryInBox = INPUTS(0)
  val mintResolverInBox = INPUTS(1)

  // nfts
  val registryNft = fromBase16("$registryNft")
  val mintResolverNft = fromBase16("$mintResolverNft")

  // validation
  val isMintResolverTx = registryInBox.tokens(0)._1 == registryNft &&
    mintResolverInBox.tokens(0)._1 == mintResolverNft

  sigmaProp(isMintResolverTx)
}

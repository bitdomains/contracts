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
  // The buyer must submit a commitment box in the transaction to prevent frontrunning.
  // A commitment is valid if:
  //  - The box was created more than `MinCommitmentAge` blocks ago & less than `MaxCommitmentAge` blocks ago.
  //  - The R4 of the box contains a value of blake2b256(secret ++ encoded(buyerPk) ++ label ++ tld ++ address) - commitment hash.
  //
  //   Input                      |  Output        |  Data-Input
  // ------------------------------------------------------------
  // 0 Registry                   |  Registry      |
  // 1 MintResolver               |  MintResolver  |
  // 2 MintResolverRequest        |  Resolver      |
  // 3 MintResolverCommitment     |                |
  //
  // [2] Refund request
  // This box can be spent to retrieve the contained ERGs if the spender can `proveDlog` the PK
  // contained in R6. I.e sign the transaction using the associated SK.
  //
  // REGISTERS
  //  R4: (Coll[Byte])    Commitment secret.
  //  R5: (Coll[Byte])    Commitment box id.
  //  R6: (GroupElement)  PK of the buyer, transaction must be signed with the associated SK to ensure
  //                        the buyer can spend/use the Resolver.
  //  R7: (Coll[Byte])    Label (name) that is used to resolve an address.
  //  R8: (Coll[Byte])    Registrar/TLD, "erg" for example.
  //  R9: (Coll[Byte])    Address to resolve to, this should be set based on the TLD.
  //                        For example if TLD is "erg" an Ergo address, if TLD is "ada" a Cardano address.
  //
  // VALUE
  //  Box must contain enough ERG to cover the cost of the `MintResolver` transaction.


  // needs to contain all the parameters currently on `MintResolver`
  // needs to include the amount to pay for the name + fees
  // needs to contain the commitment boxes id so it can be found for the `MintResolver` action
  // in box at index 3 should be a commitment, this box should be index 2

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

  // buyer can spend the box to get a refund/cancel the request
  val isBuyer = proveDlog(buyerPk)

  isBuyer || sigmaProp(isMintResolverTx)
}
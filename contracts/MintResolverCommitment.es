{
  // Mint resolver commitment script
  //
  // TRANSACTIONS
  //
  // [1] Mint Resolver
  // Creates a resolver box/nft that is used for address resolution.
  // This action is called by users to create resolvers for their specified label (name) and registrar (TLD).
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
  // [2] Rescind Commitment
  // The committer can rescind their commitment by using this box in a transaction and supplying the
  // commitment hash as context variable 0. Only the committer will know the secret used to make the
  // hash so only they will be able to correctly construct it & rescind the commitment.
  //
  // REGISTERS
  //  R4: (Coll[Byte]) Commitment hash: blake2b256(secret ++ encoded(buyerPk) ++ label ++ tld ++ address).
  //
  // VARIABLES
  //  0: (Coll[Byte]) Commitment hash - allows the committer to rescind their commitment and spend this box.
  //                    Only the committer will know the secret used in the hash so only the committer
  //                    will be able to refund the committment.

  // indexes
  val registryIndex = 0
  val mintResolverIndex = 1

  // boxes
  val registryInBox = INPUTS(registryIndex)
  val mintResolverInBox = INPUTS(mintResolverIndex)

  // nfts
  val registryNft = fromBase16("$registryNft")
  val mintResolverNft = fromBase16("$mintResolverNft")

  // validation
  val isMintResolverTx = registryInBox.tokens(0)._1 == registryNft &&
    mintResolverInBox.tokens(0)._1 == mintResolverNft

  // committer can rescind commitment by spending this box
  val isCommitter = {
    val suppliedCommitment = getVar[Coll[Byte]](0)

    if (suppliedCommitment.isDefined) {
      // tx creator knew the secret value, should only be known by committer
      suppliedCommitment.get == SELF.R4[Coll[Byte]].get
    } else {
      false
    }
  }

  sigmaProp(isCommitter || isMintResolverTx)
}
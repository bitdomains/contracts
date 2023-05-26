{
  // Registry script
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
  //   Input         |  Output        |  Data-Input
  // -----------------------------------------------
  // 0 Registry      |  Registry      |
  // 1 MintResolver  |  MintResolver  |
  // 2 Commitment    |  Resolver      |
  //
  // REGISTERS
  //  R4: (AvlTree) Resolvers AVL tree.
  //  R5: (AvlTree) Reservations AVL tree.
  //
  // Note: `MintResolver` requires access to the Resolvers & Reservations AVL trees, store state centrally in this contract.

  // indexes
  val selfIndex = 0
  val actionIndex = 1

  // boxes
  val successorOutBox = OUTPUTS(selfIndex)
  val actionInBox = INPUTS(actionIndex)

  // nfts
  val mintResolverNft = fromBase16("$mintResolverNft")
  val resolverReservationNft = fromBase16("$resolverReservationNft")

  // registers
  val inResolversState = SELF.R4[AvlTree].get
  val outResolversState = successorOutBox.R4[AvlTree].get
  val inReservationsState = SELF.R5[AvlTree].get
  val outReservationsState = successorOutBox.R5[AvlTree].get

  // validity checks
  val validMintResolver = actionInBox.tokens(0)._1 == mintResolverNft
  val validResolverReservation = actionInBox.tokens(0)._1 == resolverReservationNft

  val validResolvers = if (!validMintResolver) {
    inResolversState.digest == outResolversState.digest // ensure resolvers are unchanged
  } else true // MintResolver script will validate/update tree

  val validReservations = if (!validResolverReservation) {
    inReservationsState.digest == outReservationsState.digest // ensure reservations are unchanged
  } else true // ResolverReservation script will validate/update tree

  val validSuccessorBox = successorOutBox.propositionBytes == SELF.propositionBytes &&
    SELF.tokens == successorOutBox.tokens &&
    validResolvers &&
    validReservations

  val validAction = validMintResolver || validResolverReservation

  sigmaProp(validSuccessorBox && validAction)
}

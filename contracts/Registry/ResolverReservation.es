{
  // Resolver Reservation action script
  //
  // TRANSACTIONS
  //
  // [1] Mint Reserved Resolver
  //
  //   Input                      |  Output               |  Data-Input
  // --------------------------------------------------------------------
  // 0 Registry                   |  Registry             |
  // 1 ResolverReservation        |  ResolverReservation  |
  // 2 ReserveResolverRequest     |  ReservedResolver     |
  //
  // VARIABLES
  //  0: (Coll[Byte]) Reservation update AVL tree proof

  // indexes
  val registryIndex = 0
  val selfIndex = 1

  // boxes
  val registryInBox = INPUTS(registryIndex)
  val registryOutBox = OUTPUTS(registryIndex)
  val successorOutBox = OUTPUTS(selfIndex)
  val requestInBox = INPUTS(2)
  val resolverOutBox = OUTPUTS(2)

  // registers
  val hashedResolverReservation = requestInBox.R4[Coll[Byte]].get
  val buyerPk = requestInBox.R5[GroupElement].get

  // scripts
  val reservedResolverScriptHash = fromBase16("$reservedResolverScriptHash")

  // nfts
  val registryNft = fromBase16("$registryNft")
  val expectedNftId = INPUTS(0).id

  // verification
  val validRegistryInBox = registryInBox.tokens(0)._1 == registryNft

  val validReservedResolverBox = {
    // valid script
    val validScript = blake2b256(resolverOutBox.propositionBytes) == reservedResolverScriptHash
    // valid registers
    val validHashedResolver = resolverOutBox.R4[Coll[Byte]].get == hashedResolverReservation
    val validBuyerPk = resolverOutBox.R5[GroupElement].get == buyerPk
    // valid nft
    val nft = resolverOutBox.tokens(0)
    val validOutNft = nft._1 == expectedNftId && nft._2 == 1L

    validScript && validHashedResolver && validBuyerPk && validOutNft
  }

  val validReservedResolverStateUpdate = {
    val reservationProof = getVar[Coll[Byte]](0).get
    val currentState = registryInBox.R6[AvlTree].get

    val insertOps: Coll[(Coll[Byte], Coll[Byte])] = Coll((hashedResolverReservation, expectedNftId))
    val expectedState = currentState.insert(insertOps, reservationProof).get
    val providedUpdatedTree = registryOutBox.R6[AvlTree].get

    expectedState.digest == providedUpdatedTree.digest
  }

  // valid funds are paid to bitdomains,etc
  val validFundsPaid = true

  val validSuccessorBox = successorOutBox.propositionBytes == SELF.propositionBytes && // script preserved
    successorOutBox.tokens == SELF.tokens // nft preserved

  sigmaProp(
    validRegistryInBox &&
    validReservedResolverBox &&
    validReservedResolverStateUpdate &&
    validFundsPaid &&
    validSuccessorBox
  )
}
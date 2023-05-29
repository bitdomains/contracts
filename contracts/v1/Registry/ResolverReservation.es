{
  // Resolver Reservation action script
  //
  // TRANSACTIONS
  //
  // [1] Mint Reserved Resolver
  //
  // Create a resolver (label ++ tld) reservation that can be later minted for a real resolver.
  // A reservation is required to prevent frontrunning.
  //
  // Note that it is possible to bruteforce names being reserved but this should only be
  // feasible for "high quality" names and the assumption I have is that these names
  // will be highly contested anyway and would-be attackers would just mint them directly instead
  // of attempting to frontrun. Random names that users may want to reserve aren't feasible
  // to frontrun.
  //
  // Buyers MUST ensure the TLD is valid and naming rules are followed in the reserveration request hash.
  // Otherwise a reserved resolver could be minted that is unclaimable and unrefundable.
  //
  //   Input                      |  Output               |  Data-Input
  // --------------------------------------------------------------------
  // 0 Registry                   |  Registry             |
  // 1 ResolverReservation        |  ResolverReservation  |
  // 2 ReserveResolverRequest     |  ReservedResolver     |
  //
  // TOKENS
  // 0: NFT uniquely identifying this `Registry` action.
  //
  // VARIABLES
  //  0: (Coll[Byte]) Check existing Resolvers AVL tree proof
  //  1: (Coll[Byte]) Reservation update AVL tree proof

  // indexes
  val registryIndex = 0

  // boxes
  val registryInBox = INPUTS(registryIndex)
  val registryOutBox = OUTPUTS(registryIndex)
  val successorOutBox = OUTPUTS(1)
  val requestInBox = INPUTS(2)
  val resolverOutBox = OUTPUTS(2)

  // registers
  val hashedResolverReservation = requestInBox.R4[Coll[Byte]].get
  val buyerPk = requestInBox.R5[GroupElement].get
  val resolveAddress = requestInBox.R6[Coll[Byte]].get

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
    val validAddress = resolverOutBox.R6[Coll[Byte]].get == resolveAddress
    // valid nft
    val nft = resolverOutBox.tokens(0)
    val validOutNft = nft._1 == expectedNftId && nft._2 == 1L

    validScript && validHashedResolver && validBuyerPk && validAddress && validOutNft
  }

  val validReservedResolverStateUpdate = {
    val reservationProof = getVar[Coll[Byte]](1).get
    val currentState = registryInBox.R5[AvlTree].get

    val insertOps: Coll[(Coll[Byte], Coll[Byte])] = Coll((hashedResolverReservation, expectedNftId))
    val expectedState = currentState.insert(insertOps, reservationProof).get
    val providedUpdatedTree = registryOutBox.R5[AvlTree].get

    expectedState.digest == providedUpdatedTree.digest
  }

  // requested reserved resolver doesn't already exist as a fully minted `Resolver`
  val isNewResolver = {
    val resolverState = registryInBox.R4[AvlTree].get
    val proof = getVar[Coll[Byte]](0).get
    val exists = resolverState.contains(hashedResolverReservation, proof)

    !exists
  }

  // valid funds are paid to bitdomains,etc
  val validFundsPaid = true

  val validSuccessorBox = successorOutBox.propositionBytes == SELF.propositionBytes && // script preserved
    successorOutBox.tokens == SELF.tokens // tokens preserved

  sigmaProp(
    validRegistryInBox &&
    validReservedResolverBox &&
    validReservedResolverStateUpdate &&
    isNewResolver &&
    validFundsPaid &&
    validSuccessorBox
  )
}

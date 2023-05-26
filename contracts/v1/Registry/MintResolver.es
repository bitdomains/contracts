{
  // Mint resolver action script
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
  // 3 ReservedResolver           |                |
  //
  // VARIABLES
  //  0: (Coll[Byte]) Registrars AVL tree proof
  //  1: (Coll[Byte]) Resolvers AVL tree proof
  //  2: (Coll[Coll[Byte]]) Reservations AVL tree proof
  //                          coll[0] = tree before removing resolver hash
  //                          coll[1] = tree after removing resolver hash

  // constants
  // Could use a configuration box or something?
  val MinLabelLength = 3
  val MaxLabelLength = 15 // could probably be longer

  // indexes
  val registryIndex = 0
  val selfIndex = 1
  val resolverOutIndex = 2
  val requestInIndex = 2
  val reservationInIndex = 3

  // boxes
  val successorOutBox = OUTPUTS(selfIndex)
  val registryInBox = INPUTS(registryIndex)
  val registryOutBox = OUTPUTS(registryIndex)
  val resolverOutBox = OUTPUTS(resolverOutIndex)
  val requestInBox = INPUTS(requestInIndex)
  val reservationInBox = INPUTS(reservationInIndex)

  // registers
  val reservedResolverBoxId = requestInBox.R4[Coll[Byte]].get
  val buyerPk = requestInBox.R5[GroupElement].get
  val label = requestInBox.R6[Coll[Byte]].get
  val tld = requestInBox.R7[Coll[Byte]].get
  val hashedResolver = blake2b256(label ++ tld)
  val resolveAddress = requestInBox.R8[Coll[Byte]].get

  // scripts
  val resolverScriptHash = fromBase16("$resolverScriptHash")

  // nfts
  val expectedNftId = INPUTS(0).id
  val registryNft = fromBase16("$registryNft")

  // validity
  // valid registry in box
  val validRegistryInBox = registryInBox.tokens(0)._1 == registryNft

  val validLabel = {
    val validLength = label.size <= MaxLabelLength && label.size >= MinLabelLength
    // TODO label doesnt contain invalid characters

    validLength
  }

  val validTld = {
    val tldProof = getVar[Coll[Byte]](0).get
    val currentRegistrars = registryInBox.R4[AvlTree].get
    val hashedTld = blake2b256(tld)

    currentRegistrars.contains(hashedTld, tldProof)
  }

  val validResolverBox = {
    // valid script
    val validScript = blake2b256(resolverOutBox.propositionBytes) == resolverScriptHash
    // valid registers
    val validOwnerPk = resolverOutBox.R4[GroupElement].get == buyerPk
    val validOutLabel = resolverOutBox.R5[Coll[Byte]].get == label
    val validOutTld = resolverOutBox.R6[Coll[Byte]].get == tld
    val validAddress = resolverOutBox.R7[Coll[Byte]].get == resolveAddress
    // valid nft
    val nft = resolverOutBox.tokens(0)
    val validOutNft = nft._1 == expectedNftId && nft._2 == 1L

    validScript && validOwnerPk && validOutLabel && validOutTld && validAddress && validOutNft
  }

  val validResolverTreeUpdate = {
    val resolversProof = getVar[Coll[Byte]](1).get
    val currentResolvers = registryInBox.R5[AvlTree].get

    val insertOps: Coll[(Coll[Byte], Coll[Byte])] = Coll((hashedResolver, expectedNftId)) // expectedNftId validated in validResolverBox
    val expectedResolvers = currentResolvers.insert(insertOps, resolversProof).get
    val updatedResolvers = registryOutBox.R5[AvlTree].get

    expectedResolvers.digest == updatedResolvers.digest
  }

  val validReservationTreeUpdate = {
    val proof = getVar[Coll[Coll[Byte]]](2).get(1)

    val reservationsState = registryInBox.R6[AvlTree].get
    val removeKeys: Coll[Coll[Byte]] = Coll(hashedResolver)
    val expectedState = reservationsState.remove(removeKeys, proof).get
    val updatedReservations = registryOutBox.R6[AvlTree].get

    expectedState.digest == updatedReservations.digest
  }

  val validReservation = {
    // get expected nft for reserved resolver box
    val proof = getVar[Coll[Coll[Byte]]](2).get(0)
    val reservationsState = registryInBox.R6[AvlTree].get
    val expectedNft = reservationsState.get(hashedResolver, proof).get
    // validity checks
    val validReservationBoxId = reservationInBox.id == reservedResolverBoxId
    val validNft = reservationInBox.tokens(0)._1 == expectedNft
    val validHashedResolver = hashedResolver == reservationInBox.R4[Coll[Byte]].get // user making request knew name ++ tld, hash matches
    // ensure correct buyer pk
    // ensure correct address
  }

  val validFundsPaid = {
    val amountPaid = registryOutBox.value - registryInBox.value

    // TODO payment to contract dev
    // TODO payment to ui dev
    // TODO remaining funds to DAO
    // TODO actual price calculations
    amountPaid > 100000
  }

  // successor box valid
  val validSuccessorBox = successorOutBox.propositionBytes == SELF.propositionBytes && // script preserved
    successorOutBox.tokens == SELF.tokens // nft preserved

  sigmaProp(
    validRegistryInBox &&
    validLabel &&
    validTld &&
    validResolverBox &&
    validResolverTreeUpdate &&
    validFundsPaid &&
    validSuccessorBox
  )
}
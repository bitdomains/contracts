{
  // Mint resolver action script
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
  // VARIABLES
  //  0: (Coll[Byte]) TLD AVL tree proof (Config.R4): contains TLD
  //  1: (Coll[Byte]) Resolvers AVL tree proof (insert new resolver)
  //  2: (Coll[Byte]) Reservations AVL tree lookup for resolver hash to get nft & remove reservation hash
  //                    from Reservations AVL tree

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
  val config = CONTEXT.dataInputs(0)

  // registers
  val requestReservedResolverNftId = requestInBox.R4[Coll[Byte]].get
  val buyerProp = requestInBox.R5[SigmaProp].get
  val labels = requestInBox.R6[Coll[Coll[Byte]]].get
  val tld = labels(labels.size - 1)
  val combinedLabels = labels.flatMap({ (label: Coll[Byte]) =>
    label
  })
  val hashedResolver = blake2b256(combinedLabels)
  val resolveAddress = requestInBox.R7[Coll[Byte]].get

  // nfts
  val expectedResolverNftId = INPUTS(0).id

  // validity
  // valid registry in box
  val validRegistryInBox = registryInBox.tokens(0)._1 == fromBase16("$registryNft")

  val validConfigBox = config.tokens(0)._1 == fromBase16("$configNft")

  val validTld = {
    val tldProof = getVar[Coll[Byte]](0).get
    val currentRegistrars = config.R4[AvlTree].get
    val hashedTld = blake2b256(tld)

    currentRegistrars.contains(hashedTld, tldProof)
  }

  val validResolverBox = {
    val resolverHashes = config.R5[Coll[Coll[Coll[Byte]]]].get(1)
    // users don't need to mint the newest resolver version
    val validScript = resolverHashes.exists({ (hash: Coll[Byte]) =>
      hash == blake2b256(resolverOutBox.propositionBytes)
    })

    // valid registers
    val validOwnerProp = resolverOutBox.R4[SigmaProp].get == buyerProp
    val validLabels = resolverOutBox.R5[Coll[Coll[Byte]]].get == labels
    val validAddress = resolverOutBox.R6[Coll[Byte]].get == resolveAddress
    // valid tokens
    val nft = resolverOutBox.tokens(0)
    val validOutNft = nft._1 == expectedResolverNftId && nft._2 == 1L
    val validTokens = resolverOutBox.tokens.size == 1

    validScript &&
    validOwnerProp &&
    validLabels &&
    validAddress &&
    validOutNft &&
    validTokens
  }

  // insert resolver into Registry.resolvers avl tree
  val validResolverTreeUpdate = {
    val resolversProof = getVar[Coll[Byte]](1).get
    val currentResolvers = registryInBox.R4[AvlTree].get

    val insertOps: Coll[(Coll[Byte], Coll[Byte])] = Coll((hashedResolver, expectedResolverNftId)) // expectedResolverNftId validated in validResolverBox
    val expectedResolvers = currentResolvers.insert(insertOps, resolversProof).get
    val updatedResolvers = registryOutBox.R4[AvlTree].get

    expectedResolvers.digest == updatedResolvers.digest
  }

  // remove reserved resolver from Registry.reservation
  val validReservationTreeUpdate = {
    val proof = getVar[Coll[Byte]](2).get
    val reservationsState = registryInBox.R5[AvlTree].get
    val removeKeys: Coll[Coll[Byte]] = Coll(hashedResolver)
    val actualState = reservationsState.remove(removeKeys, proof).get
    val expectedState = registryOutBox.R5[AvlTree].get

    expectedState.digest == actualState.digest
  }

  val validReservation = {
    // get expected nft for reserved resolver box
    val proof = getVar[Coll[Byte]](2).get
    val reservationsState = registryInBox.R5[AvlTree].get
    val expectedNft = reservationsState.get(hashedResolver, proof).get
    val suppliedReservationNft = reservationInBox.tokens(0)._1

    // validity checks
    // reserved resolver in box nft matches mint request in box nft
    val validReservationBoxNft = suppliedReservationNft == requestReservedResolverNftId
    // reserved resolver in box nft matches reservation nft stored in registry
    val validNft = suppliedReservationNft == expectedNft
    // user making request knew label ++ tld
    val validHashedResolver = reservationInBox.R4[Coll[Byte]].get == hashedResolver
    val validBuyerProp = reservationInBox.R5[SigmaProp].get == buyerProp
    val validAddress =  reservationInBox.R6[Coll[Byte]].get == resolveAddress

    validReservationBoxNft &&
    validNft &&
    validHashedResolver &&
    validBuyerProp &&
    validAddress
  }

  val validFundsPaid = {
    val amountPaid = registryOutBox.value - registryInBox.value

    // TODO payment to contract dev
    // TODO payment to ui dev
    // TODO remaining funds to DAO
    // TODO actual price calculations
    true
  }

  // successor box valid
  val validSuccessorBox = successorOutBox.propositionBytes == SELF.propositionBytes && // script preserved
    successorOutBox.tokens == SELF.tokens // tokens preserved

  val validBoxes = validConfigBox && validRegistryInBox && validSuccessorBox && validResolverBox

  sigmaProp(
    validBoxes &&
    validTld &&
    validReservation &&
    validReservationTreeUpdate &&
    validResolverTreeUpdate &&
    validFundsPaid
  )
}

{
  // Config script
  //
  // TRANSACTIONS
  //
  // [1] Update TLD state
  // Creates a new TLD registrar for the registry thus allowing minting of resolvers for that TLD.
  // An example of a TLD could be "erg" to allow for "myname.erg" names or similarly "ada" for "myname.ada"
  // which would allow minting resolvers for Cardano addresses.
  //
  // This is a privlidged operation performed by registry admins.
  // TLDs have functional use - not cosmetic, so we don't want to allow arbitrary registrars.
  // It only really makes sense to have registrars for Ergo + chains that have working bridges.
  //
  //   Input    |  Output   |  Data-Input
  // -------------------------------------
  // 0 Config   |  Config   |
  // 1 Admin    |  Admin    |
  //
  // TOKENS
  //  tokens(0): configNft
  //
  // REGISTERS
  //  R4: MUT (AvlTree) TLD state tree. Maintains valid registrars such as "erg" & "ada".
  //  R5: MUT (Coll[Coll[Coll[Byte]]]) Collection of script hash versions for contracts making up the bitdomains protocol.
  //        R5[0] = ReserveResolverHashes, R5[0][0] = initial version, R5[0][1] = v1, R5[0][size-1] = latest version
  //        R5[1] = ResolverHashes, R5[1][0] = initial version ... etc
  //  R6: MUT ((Coll[Int], Coll[Byte])) Pair of prices for protocol actions in USD and the NFT id of the oracle pool providing the price feed.
  //        R6._1[0] = ResolverReservation price
  //        R6._1[1] = MintResolver price
  //  R7: MUT (Coll[(SigmaProp, Int)])  Collection of fee collector -> fee basis points pairs.
  //        R7[0] = dev
  //        R7[1] = ui (SigmaProp should be unused here as it will be provided by the UI dev at tx build time via ContextVar)
  //        R7[2] = DAO
  //
  // VARIABLES
  //  0: (Byte)       Action flag (ActionUpdateTld == 0x1)
  //  1: (Coll[Byte]) TLD name
  //  2: (Coll[Byte]) Avl insert operation proof

  // constants
  val ActionUpdateRegistrars = 0.toByte
  val ActionUpdateScriptHashes = 1.toByte
  val ActionUpdatePricing = 2.toByte
  val ActionUpdateFees = 3.toByte

  // action to perform flag
  val action = getVar[Byte](0).get

  // indexes
  val adminIndex = 1

  // boxes
  val successorBox = OUTPUTS(0)
  val adminInBox = INPUTS(adminIndex)
  val adminOutBox = OUTPUTS(adminIndex)

  val isRegistrarsUnchanged = SELF.R4[AvlTree].get == successorBox.R4[AvlTree].get
  val isScriptHashesUnchanged = SELF.R5[Coll[Coll[Coll[Byte]]]].get == successorBox.R5[Coll[Coll[Coll[Byte]]]].get
  val isPricingUnchanged = SELF.R6[(Coll[Int], Coll[Byte])].get == successorBox.R6[(Coll[Int], Coll[Byte])].get
  val isFeesUnchanged = SELF.R7[Coll[(SigmaProp, Int)]].get == successorBox.R7[Coll[(SigmaProp, Int)]].get

  val actionIdExcludeCheckMap: Coll[(Byte, Boolean)] = Coll(
    (ActionUpdateRegistrars, isRegistrarsUnchanged),
    (ActionUpdateScriptHashes, isScriptHashesUnchanged)
//    (ActionUpdatePricing, isPricingUnchanged),
//    (ActionUpdateFees, isFeesUnchanged)
  )

  def checkPreserveCondtionsForAction(action: Byte): Boolean = {
    val pairsForAction = actionIdExcludeCheckMap.filter({(a: (Byte, Boolean)) => a._1 != action})
    val actionConditions = pairsForAction.map({(a: (Byte, Boolean)) => a._2})

    allOf(actionConditions)
  }

  // validity
  val validUpdateRegistrars = {
    val cfgTldState = SELF.R4[AvlTree].get
    val proof = getVar[Coll[Byte]](2).get

    val newTldVal = getVar[Coll[Byte]](1).get
    val newTldKey = blake2b256(newTldVal)
    val insertOps: Coll[(Coll[Byte], Coll[Byte])] = Coll((newTldKey, newTldVal))

    val actualState = cfgTldState.insert(insertOps, proof).get
    val expectedState = successorBox.R4[AvlTree].get

    expectedState.digest == actualState.digest
  }

  val validSuccessor = successorBox.propositionBytes == SELF.propositionBytes &&
    successorBox.tokens == SELF.tokens

  val isAdmin = adminInBox.tokens(0)._1 == fromBase16("$adminNft")

  // not strictly needed but prevent loss of admin box in case of badly formed txn
  val validAdminBox = adminInBox.propositionBytes == adminOutBox.propositionBytes &&
    adminInBox.tokens(0) == adminOutBox.tokens(0)

  val validAction = if (action == ActionUpdateRegistrars) {
    validUpdateRegistrars
  } else false

  sigmaProp(
    isAdmin &&
    validAdminBox &&
    validSuccessor &&
    checkPreserveCondtionsForAction(action) &&
    validAction
  )
}

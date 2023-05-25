{ // Config script
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
  //  R4: MUT (AvlTree) TLD state tree. Maintains valid registrars such as "erg" & "ada"
  //
  // VARIABLES
  //  0: (Byte)       Action flag (ActionUpdateTld == 0x1)
  //  1: (Coll[Byte]) TLD name
  //  2: (Coll[Byte]) Avl insert operation proof

  // constants
  val ActionUpdateTld = 1.toByte

  // action to perform flag
  val action = getVar[Byte](0).get

  // indexes
  val adminIndex = 1

  // boxes
  val successorBox = OUTPUTS(0)
  val adminInBox = INPUTS(adminIndex)
  val adminOutBox = OUTPUTS(adminIndex)

  // nfts
  val adminNft = fromBase16("$adminNft")

  // configuration
  val cfgTldState = SELF.R4[AvlTree].get

  // validity
  val validUpdateTld = {
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

  val isAdmin = adminInBox.tokens(0)._1 == adminNft

  // not strictly needed but prevent loss of admin box in case of badly formed txn
  val validAdminBox = adminInBox.propositionBytes == adminOutBox.propositionBytes &&
    adminInBox.tokens == adminOutBox.tokens

  val validAction = if (action == ActionUpdateTld) {
    validUpdateTld
  } else false

  sigmaProp(
    isAdmin &&
    validAdminBox &&
    validSuccessor &&
    validAction
  )
}
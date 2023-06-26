{
  // Resolver script
  //
  // TRANSACTIONS
  //
  // [1] Update resolution address
  // To update the address that this resolver resolves to simply send the box to self with everything
  // preserved except R6 updated.
  //
  // The value of R6 should be the address including network encoding, for
  // example on ergo: 3WwhifgHTu7ib5ggKKVFaN1J6jFim3u9siPspDRq9JnwcKfLuuxc
  //
  // Or if the resolver is for Cardano: addr1qyht4ja0zcn45qvyx477qlyp6j5ftu5ng0prt9608dxp6l2j2c79gy9l76sdg0xwhd7r0c0kna0tycz4y5s6mlenh8pq4jxtdy
  //
  //   Input         |  Output        |  Data-Input
  // -----------------------------------------------
  // 0 Resolver      |  Resolver      |
  //
  // [2] Transfer ownership
  // To transfer ownership send the box to self with R4 updated to the sigma proposition of the new owner.
  //
  // The new owner should ensure the resolver address is updated accordly otherwise funds
  // will continue to go to the previous owner.
  //
  //   Input         |  Output        |  Data-Input
  // -----------------------------------------------
  // 0 Resolver      |  Resolver      |
  //
  // REGISTERS
  //  R4: MUT   (SigmaProp)     Owners sigma proposition.
  //  R5: CONST (Coll[Byte])    Label (name) that is used to resolve an address.
  //  R6: CONST (Coll[Byte])    Registrar/TLD, "erg" for example.
  //  R7: MUT   (Coll[Byte])    Address to resolve to, this should be set based on the TLD.
  //                              For example if TLD is "erg" an Ergo address, if TLD is "ada" a Cardano address.
  //  R8: MUT   (AvlTree)       Subresolver AVL tree. subdomanName -> subResolverNftId mappings.
  //
  // TOKENS
  //  0: (CONST) Nft uniquely identifying this resoler (label ++ tld combination).

  // constants
  val ActionUpdateResolveAddress = 1.toByte
  val ActionTransferOwnership = 2.toByte
  val ActionMintSubResolver = 3.toByte

  // acion to perform flag
  val action = getVar[Byte](0).get

  val successor = OUTPUTS(0)

  // registers
  val selfOwnerProp = SELF.R4[SigmaProp].get
  val selfLabel = SELF.R5[Coll[Byte]].get
  val selfTld = SELF.R6[Coll[Byte]].get
  val selfResolveAddress = SELF.R7[Coll[Byte]].get
  val selfSubResolvers = SELF.R8[AvlTree].get
  val selfNft = SELF.tokens(0)

  // label unchanged
  val validLabel = selfLabel == successor.R5[Coll[Byte]].get
  // tld unchanged
  val validTld = selfTld == successor.R6[Coll[Byte]].get
  // nft unchanged
  val validNft = selfNft == successor.tokens(0)
  // script unchanged
  val validScript = SELF.propositionBytes == successor.propositionBytes

  val validAddressUpdate = {
    // owner shouldn't be updated for an address update action
    val validOwner = selfOwnerProp == successor.R4[SigmaProp].get
    val validSubResolvers = selfSubResolvers.digest == successor.R8[AvlTree].get.digest
    val isAddressChanged = selfResolveAddress != successor.R7[Coll[Byte]].get

    isAddressChanged && validOwner && validSubResolvers
  }

  val validOwnershipTransfer = {
    // ensure subresolver tree is cleared for new owner, 0x4ec[..] is the digest of an empty tree.
    val validSubResolvers = successor.R8[AvlTree].get.digest == fromBase16("4ec61f485b98eb87153f7c57db4f5ecd75556fddbc403b41acf8441fde8e160900")
    val ownerPropChanged = selfOwnerProp != successor.R4[SigmaProp].get

    ownerPropChanged && validSubResolvers
  }

  val validAction =
    action == ActionUpdateResolveAddress && validAddressUpdate ||
    action == ActionTransferOwnership && validOwnershipTransfer

  selfOwnerProp && sigmaProp(
    validLabel &&
    validTld &&
    validNft &&
    validScript &&
    validAction
  )
}

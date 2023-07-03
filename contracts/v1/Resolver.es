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
  // 0 Resolver      |  Resolver      |  Config
  //
  // [2] Transfer ownership
  // To transfer ownership send the box to self with R4 updated to the sigma proposition of the new owner.
  //
  // The new owner should ensure the resolver address is updated accordly otherwise funds
  // will continue to go to the previous owner.
  //
  //   Input         |  Output        |  Data-Input
  // -----------------------------------------------
  // 0 Resolver      |  Resolver      |  Config
  //
  // [3] Update script
  // Allows the owner of this `Resolver` to update the script to the latest version of the `Resolver` contract
  // as defined by the bitdomains `Config` box.
  //
  //   Input         |  Output        |  Data-Input
  // -----------------------------------------------
  // 0 Resolver      |  Resolver      |  Config
  //
  // REGISTERS
  //  R4: MUT   (SigmaProp)     Owners sigma proposition.
  //  R5: CONST (Coll[Byte])    Label (name) that is used to resolve an address.
  //  R6: CONST (Coll[Byte])    Registrar/TLD, "erg" for example.
  //  R7: MUT   (Coll[Byte])    Address to resolve to, this should be set based on the TLD.
  //                              For example if TLD is "erg" an Ergo address, if TLD is "ada" a Cardano address.
  // TOKENS
  //  0: (CONST) Nft uniquely identifying this resoler (label ++ tld combination).

  // constants
  val ActionUpdateResolveAddress = 1.toByte
  val ActionTransferOwnership = 2.toByte
  val ActionUpdateScript = 3.toByte

  // acion to perform flag
  val action = getVar[Byte](0).get

  // boxes
  val successor = OUTPUTS(0)
  val config = CONTEXT.dataInputs(0)

  // registers
  val ownerProp = SELF.R4[SigmaProp].get

  val validConfigBox = config.tokens(0)._1 == fromBase16("$configNft")

  // label, tld, nft unchanged
  val validImmutableValues = SELF.R5[Coll[Byte]].get == successor.R5[Coll[Byte]].get &&
    SELF.R6[Coll[Byte]].get == successor.R6[Coll[Byte]].get &&
    SELF.tokens(0) == successor.tokens(0)

  val isOwnerUnchanged = ownerProp == successor.R4[SigmaProp].get

  // either script is unchanged or is updated to match the script hash in config box
  val validScript = if (action == ActionUpdateScript) {
      val scriptHashes = config.R5[Coll[Coll[Byte]]].get

      isOwnerUnchanged && blake2b256(successor.propositionBytes) == scriptHashes(1)
    } else {
      SELF.propositionBytes == successor.propositionBytes
    }

  val validAddressUpdate = isOwnerUnchanged && SELF.R7[Coll[Byte]].get != successor.R7[Coll[Byte]].get

  val validOwnershipTransfer = ownerProp != successor.R4[SigmaProp].get

  val validAction =
    action == ActionUpdateScript && true || // this case is verified in `validScript`
    action == ActionUpdateResolveAddress && validAddressUpdate ||
    action == ActionTransferOwnership && validOwnershipTransfer

  ownerProp && sigmaProp(
    validConfigBox &&
    validImmutableValues &&
    validScript &&
    validAction
  )
}

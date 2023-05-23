{
  // Reserve resolver request script
  //
  // TRANSACTIONS
  //
  // [1] Resolver reservation
  //
  //   Input                      |  Output               |  Data-Input
  // -------------------------------------------------------------------
  // 0 Registry                   |  Registry             |
  // 1 ResolverReservation        |  ResolverReservation  |
  // 2 ReserveResolverRequest     |  ReservedResolver     |
  //
  // [2] Refund
  // This box can be spent to retrieve the contained ERGs if the spender can `proveDlog` the PK
  // contained in R5. I.e sign the transaction using the associated SK.
  //
  // REGISTERS
  //  R4: (Coll[Byte])    Hash of the requested reservation: blake2b256(label ++ tld).
  //  R5: (GroupElement)  PK of the buyer, can be used for refund and will be set on the final
  //                        resolver box to allow for selling/updated resolved-to address.
  //  R6: (Coll[Byte])    Address to resolve to, this should be set based on the TLD.
  //                        For example if TLD is "erg" an Ergo address, if TLD is "ada" a Cardano address.
  //
  // VALUE
  //  Box must contain enough ERG to cover the cost of the `MintResolver` transaction.

  // boxes
  val registryInBox = INPUTS(0)
  val resolverReservationInBox = INPUTS(1)

  // registers
  val buyerPk = SELF.R5[GroupElement].get

  // nfts
  val registryNft = fromBase16("$registryNft")
  val resolverReservationNft = fromBase16("$resolverReservationNft")

  // validation
  val isBuyer = proveDlog(buyerPk)

  val isReserveResolverTx = registryInBox.tokens(0)._1 == registryNft &&
    resolverReservationInBox.tokens(0)._1 == resolverReservationNft

  // refundable to buyer or must be a `Registry.ResolverReservation` tx
  isBuyer || sigmaProp(isReserveResolverTx)
}
{
  // boxes
  val registryInBox = INPUTS(0)
  val resolverReservationInBox = INPUTS(1)

  // blake2b256(name ++ tld)
  val hashedReservation = SELF.R4[Coll[Byte]].get // TODO remove this once description is added
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
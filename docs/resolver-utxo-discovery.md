## Resolver UTXO discovery

Resolver discovery is the process a wallet uses to find resolver utxos that the user owns, for example after a fresh wallet restore.

The `bitdomains` on-chain protocol tries to be implemented in such a way where this can be done succinctly and efficiently.

This document describes some trade-offs and design decisions that were made in relation to this.

### Resolver box structure

Relevant properties of a Resolver box:

1. Located at a P2S address as the boxes contract contains logic.
2. Contains a `GroupElement` that is the `PK` of the owner in `R5`, this is used to determine the Resolver owner and who can spend (trade) the Resolver and update
the resolved-to address.
3. When the Resolver UTXO is created its propositionBytes are validated to ensure the box contains the correct guard contract/script.

`Resolver.es` contract:

```scala
{
  val buyerPk = SELF.R5[GroupElement].get
  val isBuyer = proveDlog(buyerPk)

  // .. other contract logic omitted for clarity
  val otherChecks = true

  isBuyer && otherChecks
}
```

Contract that mints `Resolver.es` and checks prop bytes to ensure the resolvers script/contract is valid. (`Registry/MintResolver.es`)

```scala
{
  val resolverUtxoScriptHash = fromBase16("$scriptHash") // $scriptHash is a constant, blake2b256("Resolver.es contract")
  val resolverOutBox = OUTPUTS(0)
  val validResolverScript = blake2b256(resolverOutBox.propositionBytes) == resolverUtxoScriptHash

  // .. other contract logic omitted for clarity

  sigmaProp(validResolverScript)
}
```

#### Problems with this design

Resolver box contracts are static, this means that in order to find Resolver boxes owned by a user we need to check every single `Resolver` boxes `R5` register to see
if it's spendable. If `Ergo` and `bitdomains` gains popularity there could be 100,000s or millions of UTXOs at the P2S address that need to be checked.

Pseudocode for a discovery algorithm with this design:

```
// 30 is arbitrarily chosen, could be increased if wallet has many active addresses
Derive 30 SKs following EIP-3

// if there's 500k Resolver UTXOs in existance and a node/indexer API returns pages of 25 boxes
// it will take 20,000 network requests to check all the boxes
For each box at P2S address:
  If box is a Resolver:
    For each SK in list of generated SKs:
        if box.R5 is spendable by SK:
            Add box to list of owned Resolver boxes
```

### Possible solutions

Below I will explore some possible solutions to this issue.

#### Embedded constants

One potential solution to the problem of many boxes at the same P2S address is to embed a constant in the `Resolver` box contract so that the P2S address is unique.

If we embed the `buyerPk` as an embedded constant instead of using `R5` we can use contract templating and shortlist P2S addresses that could contain boxes owned by the user.

Pseudocode for a discovery algorithm with this design:

```
Derive 30 SKs following EIP-3

// only need to check 30 addresses for resolver boxes
for each SK in list of generated SKs:
  P2SAddress = ResolverContractTemplate(SK)
  Box = GetBoxesAtAddress(P2SAddress) // query indexer API
  if box.R5 is spendable by SK:
    Add box to list of owned Resolver boxes
```

As seen above this approach is much more efficient in terms of the amount of boxes that need to be checked but there is a drawback.

By embedding a constant and thus generating a unique P2S address/contract we can no longer use a script hash to check if the box contains the correct contract like previously:

```
val validResolverScript = blake2b256(resolverOutBox.propositionBytes) == resolverUtxoScriptHash
```

We would need to include the `Resolver` box propositionBytes in the contract that checks for validity and use something like `substConstants`.

For example the `Registry/MintResolver.es` could be updated to something like the following:

```scala
{
  val buyerPk = SELF.R5[GroupElement].get
  val resolverUtxoPropBytes = fromBase16("$scriptPropBytes") // proposition bytes of the Resolver contract
  val resolverOut = OUTPUTS(0)

  val providedResolverPropBytes = resolverOut.propositionBytes
  val expectedResolverScript = substConstants(resolverUtxoPropBytes, Coll(0), Coll(buyerPk))
  val validResolverScript = providedResolverPropBytes == expectedResolverScript

  // other contract logic omitted for clarity

  sigmaProp(validResolverScript)
}
```

This approach is less succinct as it requires including the Resolver contract propositionBytes in the minting contract instead of using a constant 32 byte hash.

Currently, the `Resolver` contract is 77 bytes in size and is quite trivial, but I plan on adding more features in the future that will increase its size (possibly considerably).

#### Allow querying indexers by box register values

Another possible solution is to add `R5` to the nodes indexer so that we can query for boxes that contain a specific `R5` value at the `Resolver`s P2S address.

This would allow similar efficiency to the previous solution without the need to embed the resolver contract propositionBytes in the minting contract.

We could continue using a 32 byte script hash to check for validity of the `Resolver` box contract and reduce on-chain data compared to the previous solution.

Pseudocode for a discovery algorithm with this design:

```
Derive 30 SKs following EIP-3

for each SK in list of generated SKs:
  GroupElement = GroupElementFromSK(SK)
  Box = GetBoxesByAddressAndR5(P2SAddress, GroupElement) // query indexer API
  if box exists:
    Add box to list of owned Resolver boxes
```

One downside here is that it limits the nodes that we can depend on to retrieve data. We will only be able to use nodes that have the indexer enabled, although this might not be a problem as wallets would likely be depending on indexer nodes anyway.

### Conclusion

Currently, I'm not completely decided on which solution to take & I have considered a few others. At this stage I think adding `R5` querying to indexer APIs
is looking like a good solution. It will allow for efficient discovery while keeping on-chain data to a minimum.

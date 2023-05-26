## Notes

## TODO

- Fees for contract developer & ui developer when minting a Resolver
- Stable pricing using oracle USD price feed
- New TLDs should probably be managed by another box to avoid needing to use them in `Registry.newRegistrar` - this requires chained tx so would require off-chain bots to be able
to perform the privlidged "add TLD operation"

## Maybe

- SubResolvers, to allow the owner of `myname.erg` to mint `pay.myname.erg`, etc

## Testing

TODO: setup test fixtures so all test cases use exactly the same tx as the success case except for the aspect under test
This is the current setup but copy+pasted for each test, use fixtures

### `Resolver.es`

- [x] label preserved
- [x] tld preserved
- [x] nft preserved
- [x] script preserved


## Reservations

- User makes a tx to reserve a name `ReservationRequest` (picked up by bots)
    - Costs the amount it takes to mint a name
    - Can be refunded to the user if they can spend it
    - Must be paid in a `Reservation` tx

### Bots pick up `ReservationRequest`s
- Creates chained txns of `Reservation`s built from `ReservationRequest`s
  - Costs the amount it takes to mint a name
    - can be refunded at a 50% fee if done within MAX RESERVATION TIME blocks (to prevent bad actors continually stealing a reservation at no cost)
    - If not actioned after MAX RESERVATION TIME blocks anyone can cancel the reservation and claim a fee (to keep reservation tree clean and prevent bad actors blocking reserve requests)
    - Can only be spent if something is known, TODO - SigmaProp?
  - R4 = name ++ tld
- `Reservation` returns a `ReservationUtxo` that contains the value
  - this is what is used to redeem a name
  - can be burnt by anyone after MAX RESV TIME to keep reservations clean

### ReservedResolver

Can be refunded for a fee
  - bots receive higher fee for this compared to "reservation expired anyone can revoke"


package bitdomains

import org.ergoplatform.ErgoAddressEncoder
import org.ergoplatform.ErgoAddressEncoder.TestnetNetworkPrefix
import scorex.crypto.hash.{Blake2b256, Digest32}

object Constants {
  val network: ErgoAddressEncoder.NetworkPrefix = TestnetNetworkPrefix

  // Registry action nfts
  val mintResolverNft =
    "ba57c53a215c8d135ff067e3e7b3a11da64690041a20f659e3a1cc14b1c7ae37"
  val adminNft =
    "94af8793a1f7b427831dcb48368ffc55c68d319d525ea24510ac38b75e280a8c"
  val configNft =
    "94af8793a1f7b427831dcb48368ffc55c68d319d525ea24510ac38b75e280ccc"
  val registryNft =
    "94af8793a1f7b427831dcb48368ffc55c68d319d525ea24510ac38b75e280a8e"
  val resolverReservationNft =
    "94af8793a1f7b427831dcb48368ffc55c68d319d525ea24510ac38b75e280a8d"

  val nftDictionary: Map[String, String] = Map(
    "mintResolverNft" -> mintResolverNft,
    "adminNft" -> adminNft,
    "registryNft" -> registryNft,
    "configNft" -> configNft,
    "resolverReservationNft" -> resolverReservationNft
  )

  private def substitute(
      contract: String,
      subs: Map[String, String]
  ): String = {
    subs.foldLeft(contract) { case (c, (k, v)) =>
      c.replace("$" + k, v)
    }
  }

  private def readContract(path: String, subs: Map[String, String]) = {
    substitute(
      scala.io.Source
        .fromFile(s"contracts/" + path, "utf-8")
        .getLines
        .mkString("\n"),
      subs
    )
  }

  lazy val resolverScript: String =
    readContract("Resolver.es", Map("configNft" -> configNft))
  private lazy val resolverScriptTree =
    Utils.compile(Map.empty, resolverScript, network)
  lazy val resolverScriptHash: Digest32 =
    Blake2b256.hash(resolverScriptTree.bytes)

  lazy val reservedResolverScript: String =
    readContract(
      "ReservedResolver.es",
      Map("mintResolverNft" -> mintResolverNft, "registryNft" -> registryNft)
    )
  private lazy val reservedResolverScriptTree =
    Utils.compile(Map.empty, reservedResolverScript, network)
  lazy val reservedResolverScriptHash: Digest32 =
    Blake2b256.hash(reservedResolverScriptTree.bytes)

  val registryScript: String =
    readContract("Registry/Registry.es", nftDictionary)

  val resolverReservationScript: String =
    readContract("Registry/ResolverReservation.es", nftDictionary)

  val mintResolverScript: String =
    readContract("Registry/MintResolver.es", nftDictionary)

  val reserveResolverRequestScript: String =
    readContract("ReserveResolverRequest.es", nftDictionary)

  val mintResolverRequestScript: String =
    readContract("MintResolverRequest.es", nftDictionary)

  val configScript: String = readContract("Admin/Config.es", nftDictionary)
}

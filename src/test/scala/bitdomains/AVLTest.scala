package bitdomains

import bitdomains.Constants.{newRegistrarNft, newRegistrarScript, registryAdminNft, registryScript}
import io.getblok.getblok_plasma.ByteConversion.{convertsArrBytes, convertsString}
import io.getblok.getblok_plasma.PlasmaParameters
import io.getblok.getblok_plasma.collections.PlasmaMap
import org.ergoplatform.{ErgoAddressEncoder, P2PKAddress}
import org.ergoplatform.ErgoAddressEncoder.TestnetNetworkPrefix
import org.ergoplatform.appkit._
import org.ergoplatform.wallet.secrets.ExtendedSecretKey
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import scorex.crypto.authds.{ADKey, ADValue}
import scorex.crypto.authds.avltree.batch.{BatchAVLProver, Insert}
import scorex.crypto.hash.{Blake2b256, Digest32}
import sigmastate.AvlTreeFlags
import sigmastate.lang.exceptions.InterpreterException

class AVLTest extends AnyFlatSpec with should.Matchers {
  val map = new PlasmaMap[Array[Byte], Array[Byte]](AvlTreeFlags.AllOperationsAllowed, PlasmaParameters.default)

  val inserts: Seq[(String, String)] = Seq(
  (
    "333724f1e5ed593ff3760e2fd14257e53320fcaba195198fe364c18317c8357a",
    "e6e95bbc282023dc0f319a10a4166099"
  )
  ,
  (
    "70dc695bd7fb4f8d40f69cd82e6d704f219b7d49efee7856a54b809019bcb281",
    "aab1db7e6768e75c360d6145563913a4"
  )
  ,
  (
    "ccdee12ce7d48bce1f2f3b237dafb03fe89109da05ad3e86ca1add4a969c6f11",
    "3e1f7320a5560a034e37a51d7f2a5187"
  )
  ,
  (
    "71eb525ad833b9ac5e35feed6a4e663125cf764069a4c4bc69e135b19b84fc20",
    "cc688ec7736770ba2cde8e8e43ef4730"
  )
  ,
  (
    "0f09f7704ed285c56154a2f7aef6eadde3e0faf9f3f07bb84393d7f83f8f9669",
    "62116adc67d12aea779cf2eadb8854ea"
  )
  ,
  (
    "e73879715ba969d9f2f2ec1970ce379fec7e770e570e9fd454714ed874039236",
    "e1fdbb6a2e7045c44d251b35822b8f9c"
  )
  ,
  (
    "66566ba505b632ce2fe70973bd2f695f62ca61edf78dd855424e1b80ed9db7ef",
    "2bb222b9e052ce69246c4461975e7fef"
  )
  ,
  (
    "c2b09394bfffe27d5c1b8ba6f67bd489b499864531c85e3c3684821f6e38729d",
    "9630dba1be5b809073bab215ad32d0ce"
  )
  ,
  (
    "fcca30ec0255f4117b71bb705f7f2e59e7222774da1dfae72c5ba032330c457a",
    "f6d131b192aa421d444fd4e5b6088428"
  )
  ,
  (
    "f8d58130ccca6981df20ec8117039e390031dbe7caee3453d18b08d8df860709",
    "6912d475249221a947c31a7c435a111f"
  )
  ,
  (
    "e0723e441695e2886e2450999a996788595dea7d25e508209a14472e06f68b36",
    "42e6f2e681d979097205822d73b9fc4c"
  )
  ,
  (
    "628181857476ed88dcbf0d77c460b8626cd968a9bdf43c1d2ea67f2acd3694d3",
    "940746265213123f4149a49ebad3030a"
  )
  ,
  (
    "4a83fefebe881f26974969c3127ebbfe711358cb89465bc3b186f895bcd61f5e",
    "e77b0de31e457f58318a65fc169060e5"
  )
  ,
  (
    "0d58e0cda79b82eddf8d518d4a1addf5a92447091d78bcecdb727ab0c81d7a7b",
    "9b01651c3362cc1964f81f3af4c73aea"
  )
  ,
  (
    "bb13eaafd61c429894899d4ed304dacc5ed6d7e962c5df7c94b0a67e73f84783",
    "5b5bb917a0f051395c425920f084fce2"
  )
  ,
  (
    "a4ca2cad4e5662c3bfdd38df5fc6dd158e125cbde07a4c549431b38da3a61c44",
    "4440d1c4032329a4408f0d84baf4ae24"
  )
  ,
  (
    "fe95b3aad90f7be6345c7e3a321e3e4b211929bdaa0dfe3d5d37410f2ff2bfbf",
    "5f7b9c94e7d96216779b2c360b9ef040"
  )
  ,
  (
    "41c55b11a7f61fd17ad228a98195383b5fc084bcc1689839bf4a8f858dbe5390",
    "b356acb04eefe3687b2cd211a5989b21"
  )
  ,
  (
    "92982f135c505f4411116f8e6a3d6f4b05468cc175df63713adca98fbb2ca21e",
    "4283e20470f9db447aa7e2ff4c75dd1f"
  )
  ,
  (
    "9f26be0d45df9f7d8193322a780823b6a42e39415f62ea8a431a3579c2d89890",
    "59405deb9da456de5a2f6048052dcfe7"
  )
  ,
  (
    "08df47d9e21a228b56872d187e7ca0033dfe93fc3afa1d1f754d6a40a427524e",
    "44063e6f03f96415569d6262775712f9"
  )
  ,
  (
    "58b5f141d5b0d4f4c991e8ebdc6e73b3fb661d6c4e29eed688b70850d37cff62",
    "659387d105856c8859ec1d78e34a65e5"
  )
  ,
  (
    "bcaa68a3b5f6fc7ae09a5f4f3156db9d556a6e858811a2973f77eee881167b69",
    "bb23952d560dace56e9bf3170b1f9090"
  )
  ,
  (
    "053d23c7067a430081b807b95b96ec914ecb974275785953236eddec3c78aa9c",
    "678370b4aa1c70170c1228e344e0fe02"
  )
  ,
  (
    "d37aeeca4ad298dccf1885946c7ea3cc573342a357b7387c23cf4d7fa7103f3b",
    "b79baf3104d094307c98ee33bfb23049"
  )
  ,
  (
    "d8795a288935436eee56b8af32504a76ac33895a827f190d69c0329285c1a4ef",
    "9db3e32a35f3f082734da766fd76afe1"
  )
  ,
  (
    "fa99ea1df1e1ec49ede3ecf66c5a9473c0bd0603eceae251771c22bd2ba84566",
    "3c77ace7964560d62915639d25b010a8"
  )
  ,
  (
    "aaa10a1f6442ab992e7b17818c72c50f3486b22a02dfc7edeb24ec93d13fc7c1",
    "98f17e60d8c8cd7237906873e569d113"
  )
  ,
  (
    "e4d5a2975d563d0ee36b581aa024549fe3cdc7a5f682d037ff64b93f835aa495",
    "7f5eb623237b593e291d6e74be684077"
  )
  ,
  (
    "3eef10eaab9e14d6329a74dde04575747e3b1b10e8f8413d1f474392d3952f88",
    "863522e89cb49b3a3f3b60c517aa742e"
  )
  ,
  (
    "2dca8ddb1ffbb4e6d25aa9ac83edd73c7c47da9c2c671a3b4f79eb69bf0314ea",
    "b8061cec7190062643ca3ec7c3bd03d9"
  )
  ,
  (
    "8c1fb9dbe4310f55128fb4bc8428957628757a42dde016a0bf15f59352eac8ed",
    "9bd7b75c06b002762e10b9cb5869c005"
  )
  ,
  (
    "63f5e594f305b10bd6d4efa17c7fc64855aa75f4241451d2440e1890c21de5d1",
    "be156e894cb55e0e36091ea96811841d"
  )
  ,
  (
    "dc021821a7b8c6c4149b1d6340c373c3d072b9eea3eb33b2a9a30ad8f0444c15",
    "2716a7a3b6a28eab0718e8ee17dbec47"
  )
  ,
  (
    "a0e3dbe575c3d386c87edf9b038ad1dc85c16673a9f9822bda12c028d3c78d17",
    "3b9381720dfe2b867428be45f0e8ab0f"
  )
  ,
  (
    "3fbbac896a6f6189f29f7440be817332de6f8d2d72f2d55d8c5f2d7a08d9ba4c",
    "a859a79147a4fa1668034d261756e48a"
  )
  ,
  (
    "cd6050353dbfe56285929b17ef824565e1e6ee9776ef2e30719296e5638f1f83",
    "096b363c35f8ae5376cae0e7a598b916"
  )
  ,
  (
    "3ee0e1bebd6f4a3b48e45a3036aa13bdf91915066af52ac082c128220072e559",
    "d3f86ffb7237259e2c366789879c5dc2"
  )
  ,
  (
    "6a7fc569c17acdd915d057c5a3a474c2a46084dc5f44604ad4c0a6c10763eecd",
    "8309468daa78abaa3342ecccef317808"
  )
  ,
  (
    "c64af22acee45a41ec9dad0c22663588437878c88bec56a31a3740f13e57a54c",
    "92243f940c1d503e4cd55170117540f7"
  )
  ,
  (
    "62c53a6093e9f3dec35db79351227c1fddceea71954a1496ba1b8299f2d27b8b",
    "cd0121eba771785f47417c877696dfe2"
  )
  ,
  (
    "15206aba0f8bf92e8ea9d4d9eb2db214c6086da88605180f090024183ca4ec21",
    "b61777769af56cd0742fc2160c9336dd"
  )
  ,
  (
    "a8296d3733c1ac6981e1027908f5a4ee3839d0fa76ffc84a5e117fd4b3c1288c",
    "11c6cc6080a083393873da0e37f41a5"
  )
  ,
  (
    "fd73b884610f1ec81d000a957ba21facb57a8dcb136b8b9228072de31772d3d5",
    "666d33ce07008ce10123f7ccef274629"
  )
  ,
  (
    "1ea6aac667fc1a99abbb04b45128d8b07b42928e399ec8a4822ab5978373fa14",
    "9f4719db94e10cdf13b77f5ef3fe7d3a"
  )
  ,
  (
    "0362af1eaf3b25f71fb1db4775ae57264e5fc9f933faa3f5bb2012c95d8754cc",
    "95b4775334f2d4120b6ea12e9d75c632"
  )
  ,
  (
    "f1cb97a2f42f497e2292f1faeee22996c4e5da3c02beb757417125338ed2d28b",
    "e62b628a0d52477a16fb486f7a5ee886"
  )
  ,
  (
    "fe4938a1c6a505c8180f15907672eebc736014e795d6f92ae465848d92210615",
    "55af2c2e1cf22737595f230b0edeee42"
  )
  ,
  (
    "6e9c564e1190f821aae7326dee8a31b16dd53216dd724011f21c09c0ac7909bc",
    "84d7eea349dc6c676f5c05c70a6c66a0"
  )
  ,
  (
    "f139bc505fa76567197a197307cf668b3c8a858b40d3fd54d66099b9baa0fde4",
    "25ac61e586c276561b28a34e9421e32a"
  )
  ,
  (
    "78ac97b232880841dc4d417274486f5db9e3cb5a84d44a94624ecf2e77d1eeea",
    "caf7061b8cc30f08792ebbf4ee2333cf"
  )
  ,
  (
    "369b593ae971613636048143b39d895148aca2eed84f91bba2a03009f2e30c68",
    "97d584cff7c21d040b1a10783998ff7"
  )
  ,
  (
    "419b1c45fcb6080fe8d091474e71b413974e2848dd1c823c8e809fb1d71f0f23",
    "d77a5e35a4455a3e2036fa9d4a45cf11"
  )
  ,
  (
    "75c98f00c72fd894663d0cbd7f25d6aa890d76e1091eebb0a2d888e7be20e054",
    "8c23c50f92960f1a125313050812858f"
  )
  ,
  (
    "c1b74d2971a2a0d09896d7f903692c71c8bc6a1b745d3fc4e70065d4beed4b36",
    "d2f84c70fa4d85245183efb8390e03a8"
  )
  ,
  (
    "48f974117ec1de3898f40dd7d1de774b17dfcc4f3f5b97caab8327a13b51e7e6",
    "697ff127028aa8c3218abf370957f8f1"
  )
  ,
  (
    "336e172aabe748ba306f5527f7b0c808132a6b9bb5c8693aca8c6debdb638ed1",
    "45038d28c5bafd4209a47003a9d64fa6"
  )
  ,
  (
    "47f54faf228c8c7a95a03223db943693d4a64cf0fc3fb45ea0f8daf18034ecbf",
    "d30b3bfe4307ee2f4031a5fc836289d7"
  )
  ,
  (
    "5cb7a321448962d35dfbc168ffc5e372c07879592027571b6d4e6f15f257c871",
    "68f672a7beaf3d2a131984679a6d9b16"
  )
  ,
  (
    "e0ccef093f197747577bfbe4688d06425976dc94c41aff62b4a02ce4cf201585",
    "167320c01294b05a3ee0335d79e2f28"
  )
  ,
  (
    "3e312123cc657dd53ad9ead5f7d883a199d3ab8fe1ea8f9bf31ab4c332c1d5d3",
    "4759442ed6afee0a65f1121f0536dbea"
  )
  ,
  (
    "2f94e8d04f92deb67ec1cf40fd95111134498a239e860da64f42ac65cf9a4fa0",
    "57a074f6e979e0684eca779377ac9c20"
  )
  ,
  (
    "4dd254290bb99e72cb8ef55c07328dd6e8c09da2460de02dfa20c57c990db64a",
    "b7b82ec4ae2e7dd045ac127321d4d9c5"
  )
  ,
  (
    "a85ad9d6d5b02e425a36eeca897356dea45b0899218cd4fdc549871c060b7875",
    "1a0ac99e765d076b31a71326d98a12fc"
  )
  ,
  (
    "2e36ffa9b0560fb5bd36fe3cc18be9fffab368ae97689db29162a5b25bb8defe",
    "11ff39bf235e45f5754aa8e6304ccb30"
  )
  ,
  (
    "e0370a1237d4d3bd28386115122b12d3eda2b82545d9ada6ea8fdb999901db28",
    "2e2b9d3f779a1a8740ccb5d02f9f8a42"
  )
  ,
  (
    "256098566aab2d640ccf672e7702031ade9b457ca9d77b968bc45d4ebe37f7a4",
    "9abf1d8934da44465a586d4268f138ee"
  )
  ,
  (
    "75dd6e9655c2a170c6d86da6cc2f5f7560e3825cf18f50a12c11b25ef76b1027",
    "02bd8d8320bde8c0146dadf4082f6cae"
  )
  ,
  (
    "19a1b37e6071f158794bd3f71f050f986dc2b20b5f7e2c73349a43ed2fc95632",
    "a66fcb935a9a6e8901d4d19765b225c0"
  )
  ,
  (
    "325e4578fb95d9846b940ee082a9e2742e220ac7b2d2ad33fd22129ff852340d",
    "fe1e86e1cd7d6fc5063f1c0dba350942"
  )
  ,
  (
    "432219d452dbf20bcb0580950de0e8d8f215d581eea13cfdf83a6c52abbc8cfb",
    "5ace91de860e0bf653eedfe5604193fc"
  )
  ,
  (
    "63b426ea793d902b649d92a0e8101cc29bef28f129b04d41ead32d6571f6a86c",
    "1a78cae401352839223dee36395a37e0"
  )
  ,
  (
    "2b75812d6b376bd8ee850e2a9ee9e62e9e183f665377cfc28e0d5a4132e23e78",
    "5044374f9f47c2fe7e99cd02d78f19a4"
  )
  ,
  (
    "d1a913bb6ed0009717864aee5f3724ae1b76c0767e407807d2ca3a074bdb991e",
    "8747502115327b0b63139c9362a1d232"
  )
  ,
  (
    "972af09a09ce7197e75c8f202d35e61fbeda2b5a80fa58ba95ae77dec094af9a",
    "0d85cb63ad0fa9a350dcefe97b7db1b0"
  )
  ,
  (
    "df272eb60444fc1b9c14f52662b10c2c2aba8ca6761f83943c81dfcbd20a6636",
    "d4f6dffc036582bd659cda3f25adb5f0"
  )
  ,
  (
    "4db1cd1ac6a4eb117d2837766727a33a7039e862fb5df7fe0c37dcd22798d0fd",
    "5ce9de9b38f34a8a3b8f677274c7ccae"
  )
  ,
  (
    "26340a0aed14a459bd3c8b4f2e0092736701c35127d0bfc54fc04143a0f68f49",
    "f4e2b49037db6ec11670e506bf2a4e54"
  )
  ,
  (
    "70340cdcad3993558b5da5d759a6f76579f30ce0ae0535758eb853848dfa8038",
    "0a7c40d8bdef0ba542f6a502728ce005"
  )
  ,
  (
    "59009a7346956fbd5f65527b757b79dcd6ede5778cc1d36933e8f8813ebdae6c",
    "5eeaab358a18c5947e098bcfc698adfc"
  )
  ,
  (
    "9373150388beb70d1cb486b453d765c4b882cd5e1fe8c4b0a1da71f8f2bcfffc",
    "bab707b6ac8ca2774ad680b806ab0f9f"
  )
  ,
  (
    "28f68dcdb534e96094e28936cec858d939f0aa21cac2d5bab87fa61357d88196",
    "964d3ac980d9dfbe3ef2ab1161a81a4f"
  )
  ,
  (
    "894c207c3f92fce8087000ff5242326968f0a2f2a07ca821258f6a639a92741b",
    "b7300e02c9e0bf264b6f68d911b48ae"
  )
  ,
  (
    "ee4248dd48eebbb06c50dfd8eb8cca90ebfe3a040727ec8e1cf47ba5d2d02dcd",
    "ef2f86e52d43f5d843a232d296abf18c"
  )
  ,
  (
    "cb8800b5154e058866873b7c74fee454d7c22d7f22d2923b4d806cf662c200a8",
    "7994640e21a4c147106719719cce5f7c"
  )
  ,
  (
    "05d00696414311d892e9ff87498d9e695ef22547462dba9baa62de1ec82f1752",
    "e8a9507e195723a64e35fac71a3b9166"
  )
  ,
  (
    "0bed223a72824a24eccfe2adadca145cc6b80cb32330eef958cecaffe708f702",
    "1c5f2e4fe7930d5d46c10c2ac57bbe05"
  )
  ,
  (
    "72b6c1b9d942955d5eab90af9346a90241adf0a2d7de88ab30ed43af5023d47f",
    "944e0c961934e9881ebd8ab16cf8b2fc"
  )
  ,
  (
    "06d174e26a3db9d508bf25b5f18ae66fa9100227a71c66abab07c9e2ca304da4",
    "fc0c861631a2528f7bc5f5b3c266d980"
  )
  ,
  (
    "7d0cd821342853406cbf929904a7ab8e214a8df41bec567f62a77f917b487852",
    "5d9d21ea7d825bee3794c4a7c81b01a3"
  )
  ,
  (
    "79317254fb0c25b66f0586f813c1f6ff2164a640b25431f0519c59d2b96d68b0",
    "6243711ebd9dd51a4a8f37f0f03d17a5"
  )
  ,
  (
    "4eee20ea80e093e87f31ea498803e5e32b5ef2576d8f41a78351cabf7322f27c",
    "45864db9601a18a424ec79d202aa5360"
  )
  ,
  (
    "724e88691cca6046ff0f0d167b762127e5758672c35036ff0f8a42061ec3bb63",
    "dbb0c5dd2e243f5c5398e1cad505feb0"
  )
  ,
  (
    "dc876e92edc9d2a9e8bbc6f072110a351513c941a45fe8bfb1097bf524676d78",
    "c63d47cc5497830c289457426d4aedd9"
  )
  ,
  (
    "22e1daa558985ba30ce48214b086b3ad6401962599d52c2b5e8f8b134a8fa9d6",
    "2006c91ea4f816f30c80667057f632ce"
  )
  ,
  (
    "d9861f7a59867d5156bf76566bcfc59300683eab8816211fc336538da94c53ee",
    "c83fb6f85e44027c13cf79303e0eef43"
  )
  ,
  (
    "728c5a9605b43e0eab7c281fccd4d0a42579db51e57ed1543c880782a04aa30e",
    "7f67517578f38cca4b96ff48f15dcbb7"
  )
  ,
  (
    "c64e79c1ccf52f8c8db2095381eac5a24a4693f2919a12bc51250133654d047a",
    "36397422db755db07c8a7e0c2eed1797"
  )
  ,
  (
    "37341f63d44ad9772cabefd63d1c2f3b264897aaf9da51fa5ab539c4876f40fc",
    "e71c006765fc0d0c5e577a1121270ba2"
  )
  ,
  (
    "c99fd09b8875c102d4374ae7ae258548b41a2377921ba593776385254282dd36",
    "5b7cc052c2af1c5a46dc063b19f6b799"
  ))
//
//  def (key: String, value: String): Unit = {
//    map.insert((key, value))
//  }

  def bytesToHex(bytes: Array[Byte]): String = {
    bytes.map("%02x".format(_)).mkString
  }

  def hexToBytes(hex: String): Array[Byte] = {
    val tempBytes = BigInt(hex, 16).toByteArray
    val bytes = if (tempBytes.head == 0) tempBytes.tail else tempBytes

    bytes
  }

  "AVLTree test" should "notbe fucked" in {
    val prover = new BatchAVLProver[Digest32, Blake2b256.type](32, None)
    var opCount = 0

    for (i <- inserts) {
      val key = hexToBytes(i._1)
      val value = hexToBytes(i._2)

      prover.performOneOperation(Insert(ADKey @@ key, ADValue @@ value))

      opCount += 1
      val r = map.insert((key, value))
//      println(r)
    }

    println(s"performed $opCount operations")
    prover.digest
    val proof = bytesToHex(prover.generateProof())

    println(s"proof: $proof")

    val newKey = hexToBytes("f95d8f8958dae7957313842290d6c985e49caf7d5de63085a4edde5b0bd002a9")
    val newVal = hexToBytes("56e463c8f3614070131984679a6d9b16")

    prover.performOneOperation(Insert(ADKey @@ newKey, ADValue @@ newVal))

    val rawProof = prover.generateProof()
    val proof2 = bytesToHex(rawProof)

    println(s"proof2: $proof2")

    assert(proof2 == "0306438b25468931ab7fa11c60933e0c27bbb91f91357513979520b7312a7467ba039f4808bee75191ad78269f0f4831b4eebe2e7a7501a0c36b41bf01a24df236c403b3531cf228ec2c35ce5d70378b95d88af5f33e777cd02bf604f726ba26c27bc703bbca334727bfb616662d91311970bbf4aa914fca3a1ff6c2a4d7f17b8b60a90602f8d58130ccca6981df20ec8117039e390031dbe7caee3453d18b08d8df860709fa99ea1df1e1ec49ede3ecf66c5a9473c0bd0603eceae251771c22bd2ba84566000000106912d475249221a947c31a7c435a111f03fbaebf5b842c50cd486d23e2c41572126c7020bdff2d0c711d4ae6c98460cf320003270e8dbf573271d475b9296d04965b7b4fee58a1733957e6394f27ff15dc1250010000ff000430")
  }
}


// 0327855c89abe875bf83514d8a450c5d62a73af36772ed625e2fef29ebac55a7d503d137b39dd16c972baba3004ddcb54eb245761c1222e575a1c7c6cbbcc8f4b2760316777f4b0a6f2f35d27fbbfb7321790620b93b71b49e26d95b6a4be48b6f54ea03e93041022b0fa6afee54bdc118859de28d2ee8331422bfd0fdd1221cda70315f03824ab8d12062860d4dda315441dfed60a49162896865195b2e854be82bf2d59902c64e79c1ccf52f8c8db2095381eac5a24a4693f2919a12bc51250133654d047acb8800b5154e058866873b7c74fee454d7c22d7f22d2923b4d806cf662c200a80000001036397422db755db07c8a7e0c2eed179703303efa348fe31f22c12c4e25891f81ea1dacd81544b38c7e8f0bd6d0e2852ea300000100ff03d17f272d07200cb041927f6477aeaa603ccce16c6bbd834dc89f6ea763680a80ff000442
// 03f47eeb67d426ca1b5ea0e6da4574b6554ff3da420820cfaac74dcb8aa81c8a570333c8da8a370bc5c08b17e70b13e2f6e256011843fc2bf1907c8e42ea94178e8a037b714065302272da9a5123857a4f036d5d5f6abeab0ca08ec9cec039edd6df2e03e93041022b0fa6afee54bdc118859de28d2ee8331422bfd0fdd1221cda70315f03824ab8d12062860d4dda315441dfed60a49162896865195b2e854be82bf2d59902c64e79c1ccf52f8c8db2095381eac5a24a4693f2919a12bc51250133654d047acb8800b5154e058866873b7c74fee454d7c22d7f22d2923b4d806cf662c200a80000001036397422db755db07c8a7e0c2eed179703303efa348fe31f22c12c4e25891f81ea1dacd81544b38c7e8f0bd6d0e2852ea300000100ff030dee5541198c5d26601bdd9ac511b54a5d2ca131a6c3e750bf25ff15f04e43edff000442
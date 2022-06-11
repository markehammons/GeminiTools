package sagittarius

enum KeyStoreConfig:
  case KeyStoreGeneratorConfig[S <: Int](
    storageLocation: CWPath,
    password: String,
    kpAlgorithm: KeyPairAlgorithm[S],
    keySize: S
  )

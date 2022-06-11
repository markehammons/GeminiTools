package sagittarius

enum KeyPairAlgorithm[Sizes]:
  case RSA extends KeyPairAlgorithm[1024 | 2048]

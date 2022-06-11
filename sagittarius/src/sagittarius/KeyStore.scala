package sagittarius

import java.security.{KeyStore as JavaKeyStore}
import zio.stream.*
import zio.*
import java.security.PrivateKey
import java.security.cert.Certificate
import java.nio.file.Path
import java.nio.file.Files
import java.security.KeyStore.PrivateKeyEntry
import java.security.cert.X509Certificate
import java.security.KeyPairGenerator
import scala.util.chaining.*
import java.time.ZonedDateTime
import java.io.FileOutputStream
import java.io.OutputStream
import java.security.Security
import org.bouncycastle.jce.provider.BouncyCastleProvider

enum KeyStoreType:
  case JKS

  def typeString = this match
    case JKS => "jks"

trait KeyStore:
  def underlying: UIO[JavaKeyStore]

object KeyStore:
  private def fetchEntry(
    alias: String,
    ks: JavaKeyStore,
    password: String
  ): Option[(PrivateKey, X509Certificate)] =
    for
      _ <- if ks.containsAlias(alias) then Some(()) else None
      cert <- ks.getCertificate(alias) match
        case cert: X509Certificate => Some(cert)
        case _                     => None
      privKey <-
        ks.getEntry(
          alias,
          JavaKeyStore.PasswordProtection(password.toCharArray)
        ) match
          case pke: PrivateKeyEntry => Some(pke.getPrivateKey)
          case _                    => None
      _ = println(s"successfully fetched entry for $alias")
    yield (privKey, cert)

  private def genCert(
    alias: String,
    sr: SecureRandom
  ): Task[(PrivateKey, X509Certificate)] =
    val keys = ZIO.attempt(
      KeyPairGenerator
        .getInstance("RSA")
        .tap(_.initialize(2048))
        .pipe(kpg => kpg.generateKeyPair)
        .pipe(kp => kp.getPrivate -> kp.getPublic)
    )
    for
      _ <- ZIO.attempt(Security.addProvider(BouncyCastleProvider()))
      number <- sr.nextInt
      (privateKey, publicKey) <- keys
    yield privateKey -> X509V3CertificateBuilder.default
      .setSerialNumber(number)
      .setIssuerDn(s"CN=$alias, OU=None, O=None, L=None, C=None")
      .setNotBefore(ZonedDateTime.now.minusHours(1))
      .setNotAfter(ZonedDateTime.now.plusYears(1))
      .setSubjectDN(s"CN=$alias, OU=None, O=None, L=None, C=None")
      .setPublicKey(publicKey)
      .setSignatureAlgorithm("SHA256WithRSAEncryption")
      .generateX509Certificate(privateKey)

  val live =
    ZLayer.scoped {
      for
        sr <- ZIO.service[SecureRandom]
        conf <- ZIO.service[ServerConfig]
        ksConf <- ZIO.service[KeyStoreConfig]
        res <-
          val ks = JavaKeyStore.getInstance(KeyStoreType.JKS.typeString)
          ksConf match
            case KeyStoreConfig.KeyStoreGeneratorConfig(
                  path,
                  password,
                  alg,
                  size
                ) =>
              for
                is <-
                  if Files.isReadable(path) then
                    println("path is readable!!")
                    ZStream.fromPath(path).toInputStream
                  else ZIO.succeed(null)
                _ = ks.load(is, password.toCharArray)
                nks = JavaKeyStore
                  .getInstance(KeyStoreType.JKS.typeString)
                  .tap(_.load(null, password.toCharArray))
                _ <- ZIO.collectAll(
                  conf.aliases.map(alias =>
                    for
                      (privKey, cert) <- fetchEntry(alias, ks, password).fold(
                        genCert(alias, sr)
                      )(ZIO.succeed)
                      _ <- ZIO.attempt(
                        nks.setKeyEntry(
                          alias,
                          privKey,
                          password.toCharArray,
                          List(cert).toArray
                        )
                      )
                    yield ()
                  )
                )
                os <- ZIO.fromAutoCloseable(
                  ZIO.attempt(new FileOutputStream(path.toFile))
                )
                _ <- ZIO.attempt(nks.store(os, password.toCharArray))
              yield new KeyStore:
                def underlying: UIO[JavaKeyStore] = ZIO.succeed(nks)
      yield res
    }

  def underlying = ZIO.serviceWithZIO[KeyStore](_.underlying)

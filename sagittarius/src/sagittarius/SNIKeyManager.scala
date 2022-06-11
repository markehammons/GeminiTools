package sagittarius

import javax.net.ssl.X509ExtendedKeyManager
import java.security.Principal
import java.net.Socket
import javax.net.ssl.SSLEngine
import javax.net.ssl.ExtendedSSLSession
import javax.net.ssl.SNIHostName
import javax.net.ssl.StandardConstants
import javax.net.ssl.KeyManagerFactory

import scala.jdk.CollectionConverters.*
import scala.util.chaining.*
import javax.net.ssl.KeyManager
import java.security.KeyStore

class SNIKeyManager(keyManager: X509ExtendedKeyManager, defaultAlias: String)
    extends X509ExtendedKeyManager:
  export keyManager.{
    getServerAliases,
    chooseServerAlias,
    getCertificateChain,
    getPrivateKey
  }

  def getClientAliases(x: String, y: Array[Principal]): Array[String] = ???
  def chooseClientAlias(
    x: Array[String],
    y: Array[Principal],
    z: Socket
  ): String = ???
  override def chooseEngineClientAlias(
    x: Array[String],
    y: Array[Principal],
    z: SSLEngine
  ): String = ???
  override def chooseEngineServerAlias(
    x: String,
    y: Array[Principal],
    z: SSLEngine
  ): String =
    z.getHandshakeSession match
      case e: ExtendedSSLSession =>
        e.getRequestedServerNames.asScala
          .map(i => i -> i.getType)
          .collectFirst {
            case (n: SNIHostName, StandardConstants.SNI_HOST_NAME) =>
              Option(n.getAsciiName)
          }
          .flatten
          .filter(h =>
            getCertificateChain(h) != null && getPrivateKey(h) != null
          )
          .getOrElse(defaultAlias)

object SNIKeyManager:
  def apply(keyManagerFactory: KeyManagerFactory, defaultAlias: String) =
    keyManagerFactory.getKeyManagers
      .collectFirst { case km: X509ExtendedKeyManager =>
        new SNIKeyManager(km, defaultAlias)
      }
      .toRight(new Exception("no x509ExtendedKeyManager!!"))

  def apply(defaultAlias: String, ks: KeyStore): Array[KeyManager] =
    KeyManagerFactory
      .getInstance(KeyManagerFactory.getDefaultAlgorithm)
      .tap(_.init(ks, "secret".toCharArray))
      .pipe(kmf =>
        apply(kmf, defaultAlias).fold(_ => kmf.getKeyManagers, m => Array(m))
      )

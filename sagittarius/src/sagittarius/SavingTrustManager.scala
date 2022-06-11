package sagittarius

import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate

object SavingTrustManager extends X509TrustManager {
  def getAcceptedIssuers(): Array[X509Certificate] = Array.empty
  def checkServerTrusted(x: Array[X509Certificate], y: String): Unit = ()
  def checkClientTrusted(x: Array[X509Certificate], y: String): Unit = ()
}

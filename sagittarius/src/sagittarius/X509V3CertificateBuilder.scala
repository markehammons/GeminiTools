package sagittarius

import org.bouncycastle.x509.X509V3CertificateGenerator
import scala.util.chaining.*
import java.security.Principal
import org.bouncycastle.jce.X509Principal
import java.time.ZonedDateTime
import java.util.Date
import java.security.PublicKey
import java.security.PrivateKey

class X509V3CertificateBuilder(
  fn: X509V3CertificateGenerator => X509V3CertificateGenerator
) {
  def setSerialNumber(num: BigInt) = chain(
    _.setSerialNumber(num.abs.bigInteger)
  )
  def setIssuerDn(issuer: String) = chain(_.setIssuerDN(X509Principal(issuer)))
  def setNotBefore(dateTime: ZonedDateTime) = chain(
    _.setNotBefore(dateTime.toInstant.pipe(Date.from))
  )
  def setNotAfter(dateTime: ZonedDateTime) = chain(
    _.setNotAfter(dateTime.toInstant.pipe(Date.from))
  )
  def setSubjectDN(subject: String) = chain(
    _.setSubjectDN(X509Principal(subject))
  )
  def setPublicKey(key: PublicKey) = chain(_.setPublicKey(key))
  def setSignatureAlgorithm(algorithm: String) = chain(
    _.setSignatureAlgorithm(algorithm)
  )
  private def chain(fnNext: X509V3CertificateGenerator => Unit) =
    new X509V3CertificateBuilder(fn.andThen(_.tap(fnNext)))
  def generateX509Certificate(privateKey: PrivateKey) =
    fn(X509V3CertificateGenerator()).generateX509Certificate(privateKey)
}

object X509V3CertificateBuilder:
  val default = new X509V3CertificateBuilder(identity)

trait X509CertificateBuilder:
  type X509Principal
  def setSerialNumber(num: BigInt): X509CertificateBuilder
  def setIssuerDn(issuer: X509Principal): X509CertificateBuilder
  def setNotBefore(dateTime: ZonedDateTime): X509CertificateBuilder
  def setNotAfter(dateTime: ZonedDateTime): X509CertificateBuilder
  def setSubjectDN(subject: X509Principal): X509CertificateBuilder
  def setPublicKey(key: PublicKey): X509CertificateBuilder

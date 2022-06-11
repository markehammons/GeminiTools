package sagittarius

import java.security.Principal
import java.security.cert.Certificate
import scala.util.chaining.*

final case class GeminiURI(
  host: String,
  parts: List[String],
  query: Option[String]
):
  def /(pathFragment: String) = copy(parts = parts :+ pathFragment)

object Host:
  def unapply(req: GeminiRequest): Option[String] = Some(req.uri.host)

object / :
  def unapply(req: GeminiRequest): Option[(GeminiRequest, String)] =
    req.uri.parts.lastOption.map(l =>
      req.copy(uri = req.uri.copy(parts = req.uri.parts.init)) -> l
    )

object */ :
  def unapply(req: GeminiRequest): Option[(GeminiRequest, List[String])] = Some(
    req.copy(uri = req.uri.copy(parts = Nil)) -> req.uri.parts
  )

object :? :
  def unapply(req: GeminiRequest): Option[(GeminiRequest, String)] =
    req.uri.query.map(q => req.copy(uri = req.uri.copy(query = None)) -> q)

object Certified:
  def unapply(req: GeminiRequest): Option[GeminiRequest] =
    req.authInfo.tap(println).map(_ => req)

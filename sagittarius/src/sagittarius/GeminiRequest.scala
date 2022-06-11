package sagittarius

import java.security.Principal
import java.security.cert.Certificate

final case class GeminiRequest(
  origin: String,
  authInfo: Option[(Principal, List[Certificate])],
  uri: GeminiURI
)

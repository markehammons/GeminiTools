package sagittarius

import java.security.Principal
import java.security.cert.Certificate

final case class GeminiRequest(
  origin: String,
  authInfo: Option[(Principal, List[Certificate])],
  uri: GeminiURI
)

extension (p: Principal) 
  def getCNName = p.getName match 
    case s"CN=$name,$_" => name
    case s"CN=$name" =>  name

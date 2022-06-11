package sagittarius

import java.net.URI
import javax.xml.stream.events.EntityDeclaration

enum Status:
  case Input(prompt: String)
  case Success[C](content: C)(using val enc: EntityEncoder[C])
  case Redirect(uri: URI)
  case TemporaryFailure(cause: String)
  case PermanentFailure(cause: String)
  case NotFound
  case ClientCertificateRequired(reasoning: String)

  def encode(using enc: EntityEncoder[Status]) = enc(this)

object Status:
  given EntityEncoder[Status] with
    def apply(s: Status) = s match
      case Input(prompt) => EntityEncoder.stringToStream(s"10 $prompt\r\n")
      case s @ Success(c) =>
        EntityEncoder.stringToStream("20 text/gemini\r\n") ++ s.enc(c)
      case Redirect(uri) =>
        EntityEncoder.stringToStream(s"30 ${uri.toASCIIString}\r\n")
      case TemporaryFailure(cause) =>
        EntityEncoder.stringToStream(s"40 $cause\r\n")
      case PermanentFailure(cause) =>
        EntityEncoder.stringToStream(s"50 $cause\r\n")
      case NotFound => EntityEncoder.stringToStream("51 Not Found\r\n")
      case ClientCertificateRequired(reasoning) =>
        EntityEncoder.stringToStream(s"60 $reasoning\r\n")

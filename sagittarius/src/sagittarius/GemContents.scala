package sagittarius

import java.net.URI
import zio.stream.ZStream
import scala.util.chaining.*
import zio.Chunk

enum GemContents:
  case Text(content: String)
  case Link(path: URI, name: String)
  case Preformatted(altText: String, contents: String)
  case H1(content: String)
  case H2(content: String)
  case H3(content: String)
  case ULItem(content: String)
  case Quote(text: String)

object GemContents:
  given e1: EntityEncoder[GemContents] with
    def apply(gc: GemContents) = gc.match {
      case Text(content)    => content ++ "\r\n"
      case Link(path, name) => s"=> ${path.toString} $name\r\n"
      case Preformatted(altText, content) =>
        s"```$altText\r\n$content\r\n```\r\n".tap(println)
      case H1(content)     => s"# $content\r\n"
      case H2(content)     => s"## $content\r\n"
      case H3(content)     => s"### $content\r\n"
      case ULItem(content) => s"* $content\r\n"
      case Quote(content)  => s"> $content\r\n"
    }.pipe(EntityEncoder.stringToStream)

  given EntityEncoder[List[GemContents]] with
    def apply(lgc: List[GemContents]) = lgc.map(e1.apply).reduce(_ ++ _)

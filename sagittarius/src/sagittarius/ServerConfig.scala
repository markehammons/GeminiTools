package sagittarius

import zio.Tag

final case class ServerConfig(port: Int, aliases: List[String])

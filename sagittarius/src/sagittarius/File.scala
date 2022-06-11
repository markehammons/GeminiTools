package sagittarius

import zio.stream.*

object File:
  def fromPath(path: RPath): (String, UStream[Byte]) =
    val mimeType = path.getFileName.toString match
      case s"$_.gem" => "text/gemini"

    mimeType -> ZStream
      .fromPath(path)
      .catchAll(_ => ZStream.fromIterable("...".getBytes))

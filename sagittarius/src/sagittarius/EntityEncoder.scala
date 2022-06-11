package sagittarius

import zio.stream.UStream
import zio.stream.ZStream

trait EntityEncoder[A]:
  def apply(a: A): UStream[Byte]

object EntityEncoder:
  def stringToStream(str: String) = ZStream.fromIterable(str.getBytes("UTF-8"))
  given EntityEncoder[UStream[Byte]] with
    def apply(a: UStream[Byte]) = a

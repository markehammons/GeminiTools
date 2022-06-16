package sagittarius

import zio.stream.*
import zio.*
import java.io.IOException
import zio.stm.ZSTM

object File:
  def parseMimetype(name: String) = name match
    case s"$_.gem" | s"$_.gemini" => ZIO.succeed("text/gemini")
    case _                        => ZIO.succeed("text/text")
  def fromPath(path: Task[RPath]): UIO[(String, Status)] =
    val task = for
      p <- path
      mimeType = p.getFileName.toString match
        case s"$_.gem" => "text/gemini"
      stream = ZStream.fromPath(p).catchAll(t => ZStream.never)
    yield mimeType -> stream

    task
      .map((m, s) => m -> Status.Success(s))
      .catchAll(t => ZIO.succeed("" -> Status.NotFound))

  def fromResource(loc: String): Task[Status] =
    ZIO.scoped{
      val task = for
        mimeType <- parseMimetype(loc)
        _ <- ZIO.debug(s"opening resource $loc")
        is <- ZIO.fromAutoCloseable(ZIO.fromOption(Option(this.getClass.getClassLoader.getResourceAsStream(loc))))
        contents <- ZStream
          .fromInputStream(
            is
          ).runCollect.map(c => String(c.toArray))
      yield Status.Success(contents)

      task.catchAll(_ => ZIO.succeed(Status.NotFound))
    }

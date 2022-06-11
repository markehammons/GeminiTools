package sagittarius

import zio.stream.*
import zio.*
type Route[R] = URIO[R, PartialFunction[GeminiRequest, Stream[Throwable, Byte]]]

object Route:
  def needful[R](
    pf: PartialFunction[GeminiRequest, URIO[R, Status]]
  )(using Tag[R]): Route[R] =
    for r <- ZIO.service[R]
    yield pf.andThen(rio =>
      ZStream.fromZIO(rio).provideEnvironment(ZEnvironment(r)).flatMap(_.encode)
    )

  def apply(pf: PartialFunction[GeminiRequest, Status]): Route[Any] =
    ZIO.succeed(pf.andThen(_.encode))

object CertifiedRoute:
  def apply(
    pf: PartialFunction[GeminiRequest, Stream[Throwable, Byte]]
  ): Route[Any] = ZIO.succeed {
    case Certified(pf(g)) => g
    case pf(_) =>
      ZStream.fromIterable(
        "60 Client Certificate Required\r\n".getBytes("UTF-8")
      )
  }

extension [R](r: Route[R])
  def <++>[S](o: Route[S]): Route[R & S] =
    r.flatMap(pf1 => o.map(pf2 => pf1.orElse(pf2)))

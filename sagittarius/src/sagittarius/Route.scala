package sagittarius

import zio.stream.*
import zio.*
import scala.annotation.targetName
type Route[R] = URIO[R, PartialFunction[GeminiRequest, UStream[Byte]]]

object Route:
  def needful[R](
    pf: PartialFunction[GeminiRequest, RIO[R, Status]]
  )(using Tag[R]): Route[R] =
    for r <- ZIO.service[R]
    yield pf.andThen(rio =>
      ZStream.fromZIO(rio).provideEnvironment(ZEnvironment(r)).flatMap(_.encode).catchAll(t => Status.TemporaryFailure(t.getMessage).encode)
    )

  def apply(pf: PartialFunction[GeminiRequest, Status]): Route[Any] =
    ZIO.succeed(pf.andThen(_.encode))

  @targetName("taskApply")
  def apply(pf: PartialFunction[GeminiRequest, Task[Status]]): Route[Any] = ZIO.succeed(pf.andThen(task => ZStream.fromZIO(task).flatMap(_.encode).catchAll(t => Status.TemporaryFailure(t.getMessage).encode)))

object CertifiedRoute:
  def apply(
    pf: PartialFunction[GeminiRequest, UStream[Byte]]
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

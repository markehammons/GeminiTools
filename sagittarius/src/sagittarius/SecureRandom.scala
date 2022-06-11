package sagittarius

import zio.*
import java.security.{SecureRandom as JSecureRandom}

trait SecureRandom:
  def useUnderlying(f: JSecureRandom => Unit): Task[Unit]
  def nextInt: UIO[Int]

object SecureRandom:
  val live = ZLayer.succeed {
    new SecureRandom:
      val underlying = JSecureRandom()
      def useUnderlying(f: JSecureRandom => Unit): Task[Unit] =
        ZIO.attempt(f(underlying))
      def nextInt: UIO[Int] = ZIO.succeed(underlying.nextInt())
  }

package sagittarius

import zio.stream.ZStream
import zio.*
import java.net.URI
import zio.stream.ZSink
import java.security.KeyPair
import java.security.KeyPairGenerator
import scala.util.chaining.*
import org.bouncycastle.x509.X509V3CertificateGenerator
import org.bouncycastle.jce.X509Principal
import java.time.ZonedDateTime
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLServerSocket
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLHandshakeException
import java.net.SocketException
import java.io.InputStreamReader
import java.io.Reader
import javax.net.ssl.ManagerFactoryParameters
import java.security.Principal
import java.security.cert.Certificate
import scala.util.Try
import java.security.cert.X509Certificate
type UserInfo = (GeminiURI, Option[(Principal, Array[Certificate])])
trait GeminiServer:
  def handleRoutes[R](fn: Route[R])(using Tag[R]): RIO[R, Unit]

object GeminiServer:
  def handleRoutes[R](fn: Route[R])(using
    Tag[R & GeminiServer]
  ): RIO[R & GeminiServer, Unit] = ZIO.serviceWithZIO(_.handleRoutes(fn))
  val live = ZLayer.scoped {
    for
      sr <- ZIO.service[SecureRandom]
      keyStore <- KeyStore.underlying
      myKm = SNIKeyManager("localhost", keyStore)
      tm = TrustManagerFactory
        .getInstance(TrustManagerFactory.getDefaultAlgorithm)
        .tap(_.init(keyStore))
      sc = SSLContext.getInstance("TLSv1.3")
      _ <- sr.useUnderlying(
        sc.init(myKm, Array(SavingTrustManager: TrustManager), _)
      )
      serverSocket <- ZIO.fromAutoCloseable(
        ZIO.attempt(
          sc.getServerSocketFactory
            .createServerSocket(1965)
            .asInstanceOf[SSLServerSocket]
        )
      )
    yield new GeminiServer:
      val inputSink = ZSink
        .collectAll[Char]
        .map(_.toArray)
        .map(String.apply(_))
        .mapZIO(s => ZIO.attempt(URI(s)))
        .map(uri =>
          GeminiURI(
            uri.getHost,
            uri.getPath.split("/").toList,
            Option(uri.getQuery)
          )
        )
      def handleRoutes[R](fn: Route[R])(using Tag[R]) =
        val stream = 
          for 
            scope <- ZStream.scoped(ZIO.acquireReleaseExit(Scope.make)((scope, exit) => scope.close(exit)))
            socket <- ZStream.scoped(ZIO.fromAutoCloseable(ZIO.succeed(serverSocket)))

            connection <- ZStream
                .repeatZIO(
                  ZIO.attemptBlockingCancelable(
                    socket.accept.asInstanceOf[SSLSocket]
                  )(ZIO.succeed(socket.close)).pipe(scope.extend(_))
                )
          yield connection

        stream
          .mapZIOPar(8)(comSocket =>
            ZIO.scoped {
              for {
                scope <- ZIO.scope
                _ <- scope.extend(ZIO.fromAutoCloseable(ZIO.succeed(comSocket)))
                clientAddress <- ZIO.succeed(comSocket.getInetAddress)
                _ <- ZIO.attempt(comSocket.setWantClientAuth(true))
                _ <- ZIO.attempt(comSocket.startHandshake)
                peerAuth <- ZIO.succeed(
                  Try(comSocket.getSession)
                    .map(s =>
                      s.getPeerPrincipal -> s.getPeerCertificates.toList
                    )
                    .toOption
                )
                _ <- ZIO.log(peerAuth.toString)
                _ <- ZIO.log("comSocket allocated")
                uri <- ZStream
                  .fromReaderScoped(
                    ZIO.succeed(InputStreamReader(comSocket.getInputStream))
                  )
                  .tap(c => ZIO.log(c.toString))
                  .takeWhile(c => c != '\r' && c != '\n')
                  .run(inputSink)
                pf <- fn
                _ <- ZIO.log("preparing response")
                stream = pf.applyOrElse(
                  GeminiRequest(clientAddress.toString, peerAuth, uri),
                  _ => ZStream.fromIterable("51 NotFound\r\n".getBytes("UTF-8"))
                )
                _ <- stream.run(
                  ZSink.fromOutputStreamScoped(
                    ZIO.succeed(comSocket.getOutputStream)
                  )
                )
                _ <- ZIO.log("responded")
              } yield ()
            }
          )
          .catchSome {
            case e: SSLHandshakeException =>
              ZStream.logErrorCause(Cause.fail(e))
            case s: SocketException => ZStream.logErrorCause(Cause.fail(s))
            case e: Throwable =>
              ZStream.logErrorCause(
                Cause.fail(e)
              )
          }
          .runDrain
  }

  val default = SecureRandom.live >+> KeyStore.live >+> GeminiServer.live

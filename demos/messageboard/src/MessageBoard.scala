import zio.*
import sagittarius.*
import sagittarius.GeminiServer.handleRoutes
import scala.util.chaining.*
import GemContents.*
import java.net.URI
import java.security.Security
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.time.LocalDateTime
import java.nio.file.Paths
import java.nio.file.Path
import java.nio.file.Files


object MessageBoard extends ZIOAppDefault:
  val routes = Route.needful {
    case Host("localhost") / "messageboard" => 
      for 
        ref <- ZIO.service[Ref[List[Post]]]
        posts <- ref.get
      yield Status.Success(
        List(
          posts.map(_.gemini).mkString("\n\n").pipe(Text.apply),
          Link(URI("gemini://localhost/messageboard/post"), "Post!")
        )
      )
    
    case req @ Host("localhost") / "messageboard" / "post" :? postContent =>
      for 
        ref <- ZIO.service[Ref[List[Post]]]
        post = Post(
          req.authInfo.map(_._1.getCNName).getOrElse("Anonymous"),
          postContent, 
          LocalDateTime.now
        )
        _ <- ref.update(_ :+ post)
      yield Status.Redirect(URI("gemini://localhost/messageboard"))

    case Host("localhost") / "messageboard" / "post" => 
      ZIO.succeed(Status.Input("Write your post!"))
  }


  val ksc = ZLayer.succeed(KeyStoreConfig.KeyStoreGeneratorConfig(CWPath(Paths.get("./test.jks")).getOrElse(???), "secret", KeyPairAlgorithm.RSA, 2048))
  val conf = ZLayer.succeed(ServerConfig(1965, List("localhost")))
  def run = 
    val task = for 
      _ <- ZIO.logInfo("test")
      ref <- Ref.make(List.empty[Post])
      server <- handleRoutes(
        routes.provideEnvironment(ZEnvironment(ref))
      )
    yield ()
    
    task.provideLayer((ksc ++ conf) >>> GeminiServer.default)

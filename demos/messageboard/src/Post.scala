import java.time.LocalDateTime

final case class Post(author: String, message: String, time: LocalDateTime): 
  def gemini = s"At $time, $author wrote\n\n$message"

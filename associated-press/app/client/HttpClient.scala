package client

import akka.actor.ActorSystem
import play.api.libs.ws.StandaloneWSResponse
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.Future

object HttpClient {
  implicit val system: ActorSystem = ActorSystem()
  lazy val ws: StandaloneAhcWSClient = StandaloneAhcWSClient()

  def get(
      uri: String,
      headers: Seq[(String, String)] = Seq.empty,
      parameters: Seq[(String, String)] = Seq.empty
  ): Future[StandaloneWSResponse] = {
    // on the CODE env, the preview feed will return HTTP S3 URLs, but S3 now requires
    // HTTPS... force all the URIs to HTTPS to allow the requests to complete.
    val forcedHttps = uri.replaceAll("^http://", "https://")
    ws.url(forcedHttps)
      .addHttpHeaders(headers: _*)
      .addQueryStringParameters(parameters: _*)
      .get()
  }
}

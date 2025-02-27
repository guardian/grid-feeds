package client

import org.apache.pekko.actor.ActorSystem
import play.api.libs.ws.StandaloneWSResponse
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

object HttpClient {
  implicit val system: ActorSystem = ActorSystem()
  lazy val ws: StandaloneAhcWSClient = StandaloneAhcWSClient()

  private val MAX_RECURSIONS = 5

  def get(
      uri: String,
      headers: Seq[(String, String)] = Seq.empty,
      parameters: Seq[(String, String)] = Seq.empty,
      recursions: Int = 0
  ): Future[StandaloneWSResponse] = {
    if (recursions > MAX_RECURSIONS) {
      throw new IllegalArgumentException(
        s"request has redirected too many times; latest was to $uri"
      )
    }
    // on the CODE env, the preview feed will return HTTP S3 URLs, but our
    // security group only allows outbound requests on port 443...
    // force all the URIs to HTTPS to allow the requests to complete.
    val forcedHttps = uri.replaceAll("^http://", "https://")
    val request = ws
      .url(forcedHttps)
      .addHttpHeaders(headers: _*)
      .addQueryStringParameters(parameters: _*)
      .withFollowRedirects(false)
      .get()

    request.flatMap(response => {
      response.header("Location") match {
        // manage redirects ourselves, to send the new location through the forcedHttps above
        case Some(redirect)
            if response.status >= 300 || response.status < 400 =>
          get(redirect, headers, recursions = recursions + 1)
        case _ => Future.successful(response)
      }
    })(system.dispatcher)
  }
}

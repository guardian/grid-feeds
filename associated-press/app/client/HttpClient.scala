package client

import akka.actor.ActorSystem
import play.api.libs.ws.StandaloneWSResponse
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.Future

object HttpClient {
  implicit val system: ActorSystem = ActorSystem()
  lazy val ws: StandaloneAhcWSClient = StandaloneAhcWSClient()

  def get(uri: String, headers: Seq[(String, String)] = Seq.empty, parameters: Seq[(String, String)] = Seq.empty): Future[StandaloneWSResponse] = {
    ws.url(uri).addHttpHeaders(headers :_*).addQueryStringParameters(parameters :_*).get()
  }
}

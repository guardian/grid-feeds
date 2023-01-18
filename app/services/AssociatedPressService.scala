package services

import akka.actor.ActorSystem
import akka.stream.Materializer
import config.AppConfig
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.{ExecutionContext, Future}

class AssociatedPressService(
  config: AppConfig,
  implicit val system: ActorSystem,
  implicit val materializer: Materializer,
  implicit val executionContext: ExecutionContext
) {
  lazy val ws: StandaloneAhcWSClient = StandaloneAhcWSClient()

  def getAccountInfo: Future[String] = {
    ws.url(s"${config.associatedPressAPIUrl}/account/plans")
      .addHttpHeaders(("x-apikey", config.associatedPressAPIKey)).get().map { response =>
        println(response.body)
        response.body
    }
  }
}

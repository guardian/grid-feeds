package services

import akka.actor.ActorSystem
import akka.stream.Materializer
import client.HttpClient.get
import config.AppConfig
import model.FeedResponse
import play.api.libs.json.JsResult.Exception
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.{ExecutionContext, Future}

class AssociatedPressService(
  config: AppConfig,
  implicit val system: ActorSystem,
  implicit val materializer: Materializer,
  implicit val executionContext: ExecutionContext
) {
  lazy val ws: StandaloneAhcWSClient = StandaloneAhcWSClient()

  def feed: Future[FeedResponse] = get(config.associatedPressAPIDefaultFeedUrl, Seq(("x-apikey", config.associatedPressAPIKey))).map(res => parseFeedResponse(res.body))

  private def parseFeedResponse(res: String): FeedResponse = {
    Json.parse(res).validate[FeedResponse] match {
      case feedResponse: JsSuccess[FeedResponse] => feedResponse.get
      case error: JsError =>
        // TODO: log and alert on parsing errors
        throw Exception(error)
    }
  }
}

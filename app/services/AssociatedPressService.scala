package services

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.Materializer
import client.HttpClient.get
import config.AppConfig
import model.FeedResponse
import play.api.Logging
import play.api.libs.json.JsResult.Exception
import play.api.libs.json.{JsError, JsSuccess, Json}

import scala.concurrent.ExecutionContext

class AssociatedPressService(
  config: AppConfig,
  implicit val system: ActorSystem,
  implicit val materializer: Materializer,
  implicit val executionContext: ExecutionContext
) {
  val associatedPressServiceActor: ActorRef = system.actorOf(Props(new AssociatedPressServiceActor(config, executionContext)), name = "associatedPressServiceActor")

  def start(): Unit = {
    associatedPressServiceActor ! getFirstPageUrl
  }

  // TODO retrieve this from DynamoDB and fallback to config
  private def getFirstPageUrl = config.associatedPressAPIDefaultFeedUrl
}

class AssociatedPressServiceActor(config: AppConfig, implicit val executionContext: ExecutionContext) extends Actor with Logging {
  override def receive: Receive = {
    case nextPage:String =>
      logger.info(s"Calling: $nextPage")
      get(nextPage, Seq(("x-apikey", config.associatedPressAPIKey)))
        .map(response => {
          logger.info(s"Received response: ${response.body}")
          val parsedResponse =  parseFeedResponse(response.body)
          // TODO write next page value to DynamoDB
          // TODO download images in response object
          self ! parsedResponse.data.next_page
        })
  }

  private def parseFeedResponse(res: String): FeedResponse = {
    Json.parse(res).validate[FeedResponse] match {
      case feedResponse: JsSuccess[FeedResponse] => feedResponse.get
      case error: JsError =>
        // TODO: log and alert on parsing errors
        throw Exception(error)
    }
  }

}

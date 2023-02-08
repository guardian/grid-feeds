package services

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.Materializer
import client.HttpClient.get
import config.AppConfig
import model.FeedResponse
import play.api.Logging
import play.api.libs.ws.StandaloneWSResponse

import scala.concurrent.ExecutionContext

class AssociatedPressService(
  config: AppConfig,
  implicit val system: ActorSystem,
  implicit val materializer: Materializer,
  implicit val executionContext: ExecutionContext
) extends Logging {
  val associatedPressServiceActor: ActorRef = system.actorOf(Props(new AssociatedPressServiceActor(config, system, executionContext)), name = "associatedPressServiceActor")

  def start(): Unit = {
    associatedPressServiceActor ! getFirstPageUrl
  }

  // TODO retrieve this from DynamoDB and fallback to config
  private def getFirstPageUrl: String = config.associatedPressAPIDefaultFeedUrl
}

class AssociatedPressServiceActor(config: AppConfig, implicit val system: ActorSystem, implicit val executionContext: ExecutionContext) extends Actor with Logging {
  val imageUploaderServiceActor: ActorRef = system.actorOf(Props(new ImageUploaderService(config, executionContext)), name = "imageUploaderServiceActor")

  override def receive: Receive = {
    case page:String =>
      logger.info(s"Calling: $page")
      get(page, Seq(("x-apikey", config.associatedPressAPIKey))).map(handleResponse)

    def handleResponse(response: StandaloneWSResponse): Unit = {
      if(response.status == 200) {
        val feedResponse: FeedResponse = FeedResponse.parse(response.body)
        logger.info(s"Received response with ${feedResponse.data.items.length} items")
        imageUploaderServiceActor ! feedResponse.data.items
        // TODO write next page value to DynamoDB
        self ! feedResponse.data.next_page
      } else {
        logger.error(s"Received ${response.contentType} response from AP API: ${response.body}")
        // if we receive a non-200 response from the API wait 5 seconds before retrying
        Thread.sleep(5000L)
        self ! page
      }
    }
  }
}

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
        FeedResponse.parse(response.body).fold(resendRequest())(
          response => {
            logger.info(s"Received response with ${response.items.length} items")
            imageUploaderServiceActor ! response.items
            // TODO write next page value to DynamoDB
            self ! response.nextPage
          }
        )
      } else {
        logger.error(s"Received ${response.contentType} response from AP API: ${response.body}")
        resendRequest()
      }
    }

    def resendRequest(): Unit = {
      // if there is an error, we wait 5 seconds and try fetching the page again
      Thread.sleep(5000L)
      self ! page
    }
  }
}

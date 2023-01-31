package services

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.Materializer
import client.HttpClient.get
import config.AppConfig
import model.FeedResponse
import play.api.Logging

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
    case nextPage:String =>
      logger.info(s"Calling: $nextPage")
      get(nextPage, Seq(("x-apikey", config.associatedPressAPIKey)))
        .map(response => {
          val feedResponse: FeedResponse = FeedResponse.parse(response.body)
          logger.info(s"Received response with ${feedResponse.data.items.length} items")
          imageUploaderServiceActor ! feedResponse.data.items
          // TODO write next page value to DynamoDB
          self ! feedResponse.data.next_page
        })
  }
}

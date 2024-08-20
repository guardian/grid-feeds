package services

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.Materializer
import client.HttpClient.get
import config.AWS.{readFromDynamoDB, writeToDynamoDB}
import config.AppConfig
import model.FeedResponse
import play.api.Logging
import play.api.libs.ws.StandaloneWSResponse
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success, Try}
import akka.actor.Timers

class AssociatedPressService(
    config: AppConfig,
    implicit val system: ActorSystem,
    implicit val materializer: Materializer,
    implicit val executionContext: ExecutionContext
) extends Logging {
  val associatedPressServiceActor: ActorRef = system.actorOf(
    Props(new AssociatedPressServiceActor(config, system, executionContext)),
    name = "associatedPressServiceActor"
  )

  def start(): Unit = {
    associatedPressServiceActor ! getFirstPageUrl
  }

  private def getFirstPageUrl: String = {
    readFromDynamoDB(config.dynamoDBNextPageTable, "key", "nextPage") match {
      case Success(values) =>
        values.headOption.getOrElse(config.associatedPressAPIDefaultFeedUrl)
      case Failure(e) =>
        logger.error(
          "Failed to retrieve first page from dynamoDB, using default url instead",
          e
        )
        config.associatedPressAPIDefaultFeedUrl
    }
  }
}

class AssociatedPressServiceActor(
    config: AppConfig,
    implicit val system: ActorSystem,
    implicit val executionContext: ExecutionContext
) extends Actor
    with Timers
    with Logging {
  val imageUploaderServiceActor: ActorRef = system.actorOf(
    Props(new ImageUploaderService(config, executionContext)),
    name = "imageUploaderServiceActor"
  )

  override def receive: Receive = { case page: String =>
    get(page, Seq(("x-apikey", config.associatedPressAPIKey)))
      .map(handleResponse)
      .recover(e => {
        logger.error(s"Request failed $page", e)
        resendRequest()
      })

    def handleResponse(response: StandaloneWSResponse): Unit = {
      if (response.status == 200) {
        FeedResponse
          .parse(response.body)
          .fold(resendRequest())(response => {
            logger
              .info(s"Received response with ${response.items.length} items")
            imageUploaderServiceActor ! response.items
            writeNextPageToDynamoDB(response.nextPage) match {
              case Success(_) =>
                logger.debug(
                  s"Successfully wrote next page to table: ${response.nextPage}"
                )
              case Failure(_) =>
                logger.error(
                  s"Failed to write next page to table: ${response.nextPage}"
                )
            }
            self ! response.nextPage
          })
      } else {
        logger.error(
          s"Received ${response.status} response: ${response.body}"
        )
        resendRequest()
      }
    }

    def writeNextPageToDynamoDB(nextPage: String): Try[UpdateItemResponse] = {
      writeToDynamoDB(
        table = config.dynamoDBNextPageTable,
        keyName = "key",
        keyValue = "nextPage",
        content = nextPage
      )
    }

    def resendRequest(): Unit = {
      // if there is an error, we wait 5 seconds and try fetching the page again
      logger.error(s"Resending request $page in 5 seconds")
      timers.startSingleTimer(s"resend-$page", page, 5.seconds)
    }
  }
}

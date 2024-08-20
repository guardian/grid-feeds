package services

import org.apache.pekko.actor.Actor
import client.HttpClient.get
import config.{AWS, AppConfig}
import model.ImageItem
import play.api.Logging
import play.api.libs.ws.StandaloneWSResponse
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.model.PutObjectRequest

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

class ImageUploaderService(
    config: AppConfig,
    implicit val executionContext: ExecutionContext
) extends Actor
    with Logging {
  override def receive: Receive = { case items: Array[ImageItem] =>
    items.foreach(item => {
      get(item.downloadLink, Seq(("x-apikey", config.associatedPressAPIKey)))
        .map(res => handleResponse(item, res))
        .recover(e => {
          logger.error(s"Failed to download image: ${item.downloadLink}", e)
        })
    })
  }

  private def handleResponse(
      item: ImageItem,
      response: StandaloneWSResponse
  ): Unit = {
    if (response.status == 200) {
      if (response.contentType == "image/jpeg")
        uploadToS3(item, response.bodyAsBytes.toArray) match {
          case Success(message) =>
            logger.info(s"S3 upload success: $message")
          case Failure(e) => logger.error(s"S3 upload failure", e)
        }
      else {
        logger.warn(
          s"Received response of type ${response.contentType}, content not processed"
        )
      }

    } else {
      logger.warn(
        s"Could not download ${item.downloadLink}, received response ${response.body}"
      )
    }
  }

  private def uploadToS3(
      item: ImageItem,
      bytes: Array[Byte]
  ): Try[String] = {
    if (config.s3UploadEnabled) {
      Try {
        AWS.s3Client.putObject(
          PutObjectRequest
            .builder()
            .bucket(config.s3UploadBucketName)
            .key(s"ap/${item.fileName}")
            .build(),
          RequestBody.fromBytes(bytes)
        )
        s"filename: ${item.fileName}, content id: ${item.contentId}, download link: ${item.downloadLink}"
      }
    } else {
      Success(
        s"DRY RUN - would have uploaded image ${item.contentId}"
      )
    }
  }
}

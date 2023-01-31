package services

import akka.actor.Actor
import client.HttpClient.get
import config.{AWS, AppConfig}
import model.ItemMeta
import play.api.Logging
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.model.PutObjectRequest

import scala.collection.parallel.CollectionConverters._
import scala.concurrent.ExecutionContext

class ImageUploaderService(
    config: AppConfig,
    implicit val executionContext: ExecutionContext
) extends Actor
    with Logging {
  override def receive: Receive = { case items: Array[ItemMeta] =>
    items.par.foreach(entry =>
      get(
        entry.item.renditions.main.href,
        headers = Seq(("x-apikey", config.associatedPressAPIKey))
      )
        .map(response => {
          if (response.contentType == "image/jpeg") {
            if (config.s3UploadEnabled) {
              AWS.s3Client.putObject(
                PutObjectRequest
                  .builder()
                  .bucket(config.s3UploadBucketName)
                  .key(s"ap/${entry.item.altids.friendlykey}.jpg")
                  .build(),
                RequestBody.fromBytes(response.bodyAsBytes.toArray)
              )
            } else {
              logger.info(
                s"Would have uploaded image id ${entry.item.altids.friendlykey} with ${response.bodyAsBytes.size} bytes"
              )
            }
          }
        })
    )
  }
}

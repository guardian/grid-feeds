package services

import akka.actor.Actor
import client.HttpClient.get
import config.{AWS, AppConfig}
import model.ImageItem
import play.api.Logging
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.model.PutObjectRequest

import scala.collection.parallel.CollectionConverters._
import scala.concurrent.ExecutionContext

class ImageUploaderService(config: AppConfig, implicit val executionContext: ExecutionContext) extends Actor with Logging {
  override def receive: Receive = {
    case items:Array[ImageItem] =>
      items.par.foreach(item =>
        get(item.downloadLink, headers = Seq(("x-apikey", config.associatedPressAPIKey)))
          .map(response => {
            if (response.contentType == "image/jpeg") uploadToS3(item, response.bodyAsBytes.toArray)
            else logger.warn(s"")
          })
      )

    def uploadToS3(item: ImageItem, bytes: Array[Byte]):Unit = {
      if (config.s3UploadEnabled) {
        config.s3UploadBucketName.map(bucket => {
          AWS.s3Client.putObject(PutObjectRequest.builder()
            .bucket(bucket)
            .key(s"ap/${item.fileName}")
            .build(),
            RequestBody.fromBytes(bytes)
          )
        })
      } else {
        logger.info(s"S3 upload disabled: would have uploaded image ${item.contentId}")
      }
    }
  }
}

package config

import play.api.Configuration

class AppConfig(playConfig: Configuration) {
  private val config = playConfig.underlying

  val associatedPressAPIDefaultFeedUrl: String = config.getString("associatedPress.apiDefaultFeedUrl")
  val associatedPressAPIKey: String = config.getString("associatedPress.apiKey")

  val s3UploadEnabled: Boolean = if(config.hasPath("aws.s3UploadEnabled")) config.getBoolean("aws.s3UploadEnabled") else false
  val s3UploadBucketName: String = if(s3UploadEnabled) config.getString("aws.s3UploadBucketName") else ""
}

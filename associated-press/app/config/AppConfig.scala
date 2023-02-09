package config

import play.api.{Configuration, UnexpectedException}

class AppConfig(playConfig: Configuration) {
  private val config = playConfig.underlying

  val associatedPressAPIDefaultFeedUrl: String = config.getString("associatedPress.apiDefaultFeedUrl")
  val associatedPressAPIKey: String = config.getString("associatedPress.apiKey")

  val s3UploadEnabled: Boolean = config.getBoolean("aws.s3.uploadEnabled")
  val s3UploadBucketName: String = if(s3UploadEnabled) config.getString("aws.s3.uploadBucketName") else ""

  val dynamoDBNextPageTable: String = config.getString("aws.dynamodb.nextPageTable")
}

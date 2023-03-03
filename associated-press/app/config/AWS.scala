package config

import software.amazon.awssdk.auth.credentials.{
  AwsCredentialsProviderChain,
  InstanceProfileCredentialsProvider,
  ProfileCredentialsProvider
}
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration
import software.amazon.awssdk.core.retry.RetryPolicy
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.{
  AttributeValue,
  AttributeValueUpdate,
  GetItemRequest,
  UpdateItemRequest,
  UpdateItemResponse
}

import java.time.Duration
import scala.jdk.CollectionConverters._
import scala.util.Try

object AWS {
  val defaultRegion: Region = Region.EU_WEST_1
  val defaultProfile: String = "media-service"

  val credentials: AwsCredentialsProviderChain = AwsCredentialsProviderChain
    .builder()
    .credentialsProviders(
      InstanceProfileCredentialsProvider.create(),
      ProfileCredentialsProvider.create(defaultProfile)
    )
    .build()

  val s3Client: S3Client = S3Client
    .builder()
    .credentialsProvider(credentials)
    .region(defaultRegion)
    .build()

  val dynamoDbClient: DynamoDbClient = DynamoDbClient
    .builder()
    .credentialsProvider(credentials)
    .region(defaultRegion)
    .overrideConfiguration(
      ClientOverrideConfiguration
        .builder()
        .retryPolicy(RetryPolicy.builder().numRetries(3).build())
        .apiCallAttemptTimeout(Duration.ofSeconds(5L))
        .build()
    )
    .build()

  def writeToDynamoDB(
      table: String,
      keyName: String,
      keyValue: String,
      content: String
  ): Try[UpdateItemResponse] = {
    Try {
      dynamoDbClient.updateItem(
        UpdateItemRequest
          .builder()
          .tableName(table)
          .key(
            Map(keyName -> AttributeValue.builder().s(keyValue).build()).asJava
          )
          .attributeUpdates(
            Map(
              keyValue -> AttributeValueUpdate
                .builder()
                .value(AttributeValue.builder().s(content).build())
                .build()
            ).asJava
          )
          .build()
      )
    }
  }

  def readFromDynamoDB(
      table: String,
      keyName: String,
      keyValue: String
  ): Try[Iterable[String]] = {
    Try {
      dynamoDbClient
        .getItem(
          GetItemRequest
            .builder()
            .tableName(table)
            .key(
              Map(
                keyName -> AttributeValue.builder().s(keyValue).build()
              ).asJava
            )
            .build()
        )
        .item()
        .values()
        .asScala
        .map(value => value.s())
    }
  }
}

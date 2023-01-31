package config

import software.amazon.awssdk.auth.credentials.{
  AwsCredentialsProviderChain,
  InstanceProfileCredentialsProvider,
  ProfileCredentialsProvider
}
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.regions.Region

object AWS {
  val defaultRegion: Region = Region.EU_WEST_1
  val defaultProfile: String = "media-service"

  val credentials: AwsCredentialsProviderChain = AwsCredentialsProviderChain
    .builder()
    .credentialsProviders(
      ProfileCredentialsProvider.create(defaultProfile),
      InstanceProfileCredentialsProvider.create()
    )
    .build()

  val s3Client: S3Client = S3Client
    .builder()
    .credentialsProvider(credentials)
    .region(defaultRegion)
    .build()
}

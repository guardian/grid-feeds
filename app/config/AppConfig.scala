package config

import play.api.Configuration

class AppConfig(playConfig: Configuration) {
  private val config = playConfig.underlying

  val associatedPressAPIDefaultFeedUrl: String = config.getString("associatedPress.apiDefaultFeedUrl")
  val associatedPressAPIKey: String = config.getString("associatedPress.apiKey")
}

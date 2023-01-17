package config

import play.api.Configuration

class AppConfig(playConfig: Configuration) {
  private val config = playConfig.underlying

  val associatedPressAPIUrl: String = config.getString("associatedPress.apiUrl")
  val associatedPressAPIKey: String = config.getString("associatedPress.apiKey")
}

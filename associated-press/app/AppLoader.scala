import com.gu.conf.{ConfigurationLoader, FileConfigurationLocation, SSMConfigurationLocation}
import com.gu.{AppIdentity, AwsIdentity, DevIdentity}
import config.AWS.credentials
import play.api.ApplicationLoader.Context
import play.api._

import java.io.File

class AppLoader extends ApplicationLoader {
  def load(context: Context): Application = {
    startLogging(context)

    val defaultAppName = "associated-press-feed"
    val identity: AppIdentity = AppIdentity.whoAmI(defaultAppName, credentials).getOrElse(DevIdentity(defaultAppName))

    val loadedConfig = ConfigurationLoader.load(identity, credentials) {
      case identity: AwsIdentity => SSMConfigurationLocation.default(identity)
      case _: DevIdentity =>
        val home = System.getProperty("user.home")
        FileConfigurationLocation(new File(s"$home/.gu/$defaultAppName.private.conf"))
    }

    new AppComponents(
      context = context.copy(
        initialConfiguration = Configuration(loadedConfig).withFallback(context.initialConfiguration)
      )
    ).application
  }

  private def startLogging(context: Context): Unit = {
    LoggerConfigurator(context.environment.classLoader).foreach {
      _.configure(context.environment)
    }
  }
}

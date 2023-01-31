import play.api.ApplicationLoader.Context
import play.api._

class AppLoader extends ApplicationLoader {
  def load(context: Context): Application = {
    startLogging(context)
    new AppComponents(context).application
  }

  private def startLogging(context: Context): Unit = {
    LoggerConfigurator(context.environment.classLoader).foreach {
      _.configure(context.environment)
    }
  }
}

import config.AppConfig
import controllers.{FeedController, HealthcheckController}
import play.api.ApplicationLoader.Context
import play.api.{BuiltInComponentsFromContext, Logging}
import router.Routes
import play.filters.HttpFiltersComponents
import services.AssociatedPressService

class AppComponents(context: Context) extends BuiltInComponentsFromContext(context) with HttpFiltersComponents with Logging {
  val appConfig = new AppConfig(configuration)
  val associatedPressService = new AssociatedPressService(appConfig, actorSystem, materializer, executionContext)

  val healthcheckController = new HealthcheckController(controllerComponents)
  val feedController = new FeedController(controllerComponents, executionContext, associatedPressService)

  lazy val router = new Routes(httpErrorHandler, healthcheckController, feedController)
}
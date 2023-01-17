import controllers.HealthcheckController
import play.api.ApplicationLoader.Context
import play.api.BuiltInComponentsFromContext
import router.Routes
import play.filters.HttpFiltersComponents

class AppComponents(context: Context) extends BuiltInComponentsFromContext(context) with HttpFiltersComponents {
  val healthcheckController = new HealthcheckController(controllerComponents)

  lazy val router = new Routes(httpErrorHandler, healthcheckController)
}
package controllers

import play.api.mvc._

class HealthcheckController(val controllerComponents: ControllerComponents)
    extends BaseController {
  def healthCheck: Action[AnyContent] = Action {
    Ok("OK")
  }
}

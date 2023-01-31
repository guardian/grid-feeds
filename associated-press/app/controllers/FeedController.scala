package controllers

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.AssociatedPressService

import scala.concurrent.ExecutionContext

// TODO remove me! For testing purposes only
class FeedController(val controllerComponents: ControllerComponents, implicit val executionContext: ExecutionContext, associatedPressService: AssociatedPressService) extends BaseController {
  def start: Action[AnyContent] = Action {
    associatedPressService.start()
    Ok("Started service")
  }
}
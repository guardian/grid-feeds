package controllers

import play.api.mvc._
import services.AssociatedPressService

import scala.concurrent.ExecutionContext

class AssociatedPressController(val controllerComponents: ControllerComponents, val associatedPressService: AssociatedPressService) extends BaseController  {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  def info: Action[AnyContent] =  Action.async { _ =>
    associatedPressService.getAccountInfo.map(Ok(_))
  }
}

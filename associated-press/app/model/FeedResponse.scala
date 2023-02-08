package model

import play.api.Logging
import play.api.libs.json.{JsArray, JsValue, Json, OFormat}

import scala.util.{Failure, Success, Try}

case class FeedResponse(nextPage: String, items: Array[ImageItem])
case class ImageItem(contentId: String, fileName: String, downloadLink: String)

object FeedResponse extends Logging {
  def parse(res: String): Option[FeedResponse] = Try {
    val json = Json.parse(res)
    FeedResponse(
      nextPage = (json \ "data" \ "next_page").as[String],
      items = (json \ "data" \ "items").toOption.map(getItemArrayFromJsValue).getOrElse(Array.empty)
    )
  } match {
    case Success(response) => Some(response)
    case Failure(error) =>
      logger.error(s"Could not parse AP API response json", error)
      None
  }

  private def getItemArrayFromJsValue(jsValue: JsValue): Array[ImageItem] = {
    jsValue match {
      case jsArray: JsArray =>
        jsArray.value.toArray
          .filter(item => (item \ "item" \ "type").as[String] == "picture")
          .map(item => ImageItem(
            contentId = (item \ "item" \ "altids" \ "itemid").as[String],
            fileName = (item \ "item" \ "renditions" \ "main" \ "originalfilename").as[String],
            downloadLink = (item \ "item" \ "renditions" \ "main" \ "href").as[String]
          ))
      case _ => Array.empty
    }
  }
}

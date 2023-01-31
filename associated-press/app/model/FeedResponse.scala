package model

import play.api.libs.json.JsResult.Exception
import play.api.libs.json.{JsError, JsSuccess, Json, OFormat}

case class FeedResponse(data: Data)
case class Data(next_page: String, items: Array[ItemMeta])
case class ItemMeta(item: Item)
case class Item(uri: String, altids: AltIds, renditions: Renditions)
case class AltIds(friendlykey: String)
case class Renditions(main: Main)
case class Main(href: String, contentid: String)

object FeedResponse {
  private implicit val mainFormat: OFormat[Main] = Json.format[Main]
  private implicit val renditionsFormat: OFormat[Renditions] = Json.format[Renditions]
  private implicit val altIdsFormat: OFormat[AltIds] = Json.format[AltIds]
  private implicit val itemFormat: OFormat[Item] = Json.format[Item]
  private implicit val itemMetaFormat: OFormat[ItemMeta] = Json.format[ItemMeta]
  private implicit val dataFormat: OFormat[Data] = Json.format[Data]
  private implicit val feedResponseFormat: OFormat[FeedResponse] = Json.format[FeedResponse]

  def parse(res: String): FeedResponse = {
    Json.parse(res).validate[FeedResponse] match {
      case feedResponse: JsSuccess[FeedResponse] => feedResponse.get
      case error: JsError =>
        // TODO: log and alert on parsing errors
        throw Exception(error)
    }
  }
}

package model

import play.api.libs.json.{Json, OFormat}

case class FeedResponse(data: Data)
case class Data(next_page: String, items: Array[ItemMeta])
case class ItemMeta(item: Item)
case class Item(uri: String, renditions: Renditions)
case class Renditions(main: Main)
case class Main(href: String, contentid: String)

object FeedResponse {
  implicit val mainFormat: OFormat[Main] = Json.format[Main]
  implicit val renditionsFormat: OFormat[Renditions] = Json.format[Renditions]
  implicit val itemFormat: OFormat[Item] = Json.format[Item]
  implicit val itemMetaFormat: OFormat[ItemMeta] = Json.format[ItemMeta]
  implicit val dataFormat: OFormat[Data] = Json.format[Data]
  implicit val feedResponseFormat: OFormat[FeedResponse] = Json.format[FeedResponse]
}

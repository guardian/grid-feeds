package model

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class FeedResponseTest extends AnyFunSuite with Matchers {
  val nextPage = "https://api.ap.org/media/v2.2-preview/content/feed?qt=12345&seq=12345"
  val downloadLink = "https://api.ap.org/media/v2.2-preview/content/12345/download"
  val pictureItem: String = s"""{
    "type": "picture",
    "altids": {
      "itemid": "12345"
    },
    "renditions": {
      "main": {
        "href": "$downloadLink",
        "originalfilename": "name.jpg"
      }
    }
  }""".stripMargin

  test("should handle malformed json") {
    val input = "not json"
    FeedResponse.parse(input) should be(None)
  }

  test("should handle missing data") {
    val input = "{}"
    FeedResponse.parse(input) should be(None)
  }

  test("should extract next page url") {
    val input =
      s"""{
        "data": {
          "next_page": "$nextPage",
          "items": []
        }
      }""".stripMargin
    FeedResponse.parse(input).get.nextPage should be(nextPage)
  }

  test("should correctly parse a picture") {
    val input =
      s"""{
        "data": {
          "next_page": "$nextPage",
          "items": [
            {
              "item": $pictureItem
            }
          ]
        }
      }""".stripMargin
    val items = FeedResponse.parse(input).get.items
    items.length should be(1)
    items.head.downloadLink should be(downloadLink)
  }

  test("should filter non-image items") {
    val input =
      s"""{
        "data": {
          "next_page": "$nextPage",
          "items": [
            {
              "item": $pictureItem
            },
            {
              "item": {
                "type": "text"
              }
            }
          ]
        }
      }""".stripMargin
    val items = FeedResponse.parse(input).get.items
    items.length should be(1)
    items.head.downloadLink should be(downloadLink)
  }
}

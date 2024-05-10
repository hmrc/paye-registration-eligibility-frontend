/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package repositories

import helpers.IntegrationSpecBase
import models.DatedCacheMap
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.Filters
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap

import java.time.{LocalDateTime, ZoneOffset}
import java.time.temporal.ChronoUnit

class SessionRepositorySpec extends IntegrationSpecBase {

  lazy val repository = app.injector.instanceOf[SessionRepository]

  val docId = "id1234"
  val dataToSave = CacheMap(
    id = docId,
    data = Map(
      "data" -> Json.obj(
        "foo" -> "bar"
      )
    )
  )

  class Setup {
    await(repository.collection.deleteMany(BsonDocument()).toFuture())
    await(repository.collection.countDocuments().toFuture()) mustBe 0
    val now = LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MINUTES)
  }


  "SessionRepository" must {

    "saving data" must {

      "write the data to Mongo with a timestamp" in new Setup {

        await(repository.upsert(dataToSave)) mustBe true

        await(repository.collection.find[DatedCacheMap](Filters.eq("id", docId)).headOption()) match {
          case None => fail("CacheMap was not retrieved from Mongo")
          case Some(datedCacheMap) =>
            datedCacheMap.id mustBe docId
            datedCacheMap.data mustBe dataToSave.data
            datedCacheMap.lastUpdated.truncatedTo(ChronoUnit.MINUTES) mustBe now
        }
      }
    }

    "reading data" must {

      "read the datedCacheMap and return the CacheMap" in new Setup {

        val datedCacheMap = DatedCacheMap(docId, dataToSave.data, now)

        await(repository.collection.insertOne(datedCacheMap).toFuture())

        await(repository.get(docId)) mustBe Some(dataToSave)
      }
    }
  }
}

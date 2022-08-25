/*
 * Copyright 2022 HM Revenue & Customs
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

import models.DatedCacheMap
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{FindOneAndReplaceOptions, IndexModel, IndexOptions}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class SessionRepository @Inject()(config: ServicesConfig, mongo: MongoComponent) extends PlayMongoRepository[DatedCacheMap](
  mongoComponent = mongo,
  collectionName = config.getString("mongodb.collectionName"),
  domainFormat = DatedCacheMap.formats,
  indexes = Seq(
    IndexModel(
      ascending("lastUpdated"),
      IndexOptions()
        .name("userAnswersExpiry")
        .expireAfter(config.getInt("mongodb.timeToLiveInSeconds"), TimeUnit.SECONDS)
    )
  ),
  extraCodecs = Seq(Codecs.playFormatCodec(CacheMap.formats))
) {

  def upsert(cm: CacheMap): Future[Boolean] =
    collection.findOneAndReplace(
      filter = equal("id", cm.id),
      replacement = DatedCacheMap(cm),
      options = FindOneAndReplaceOptions().upsert(true)
    ).toFuture().map(_ => true)

  def get(id: String): Future[Option[CacheMap]] =
    collection.find[CacheMap](equal("id", id)).headOption()
}
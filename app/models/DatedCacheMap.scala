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

package models

import play.api.libs.json._
import uk.gov.hmrc.http.cache.client.CacheMap

import java.time.{Instant, LocalDateTime, ZoneOffset}

case class DatedCacheMap(id: String,
                         data: Map[String, JsValue],
                         lastUpdated: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC))

object DatedCacheMap {
  private val localDateTimeReads: Reads[LocalDateTime] =
    Reads.at[String](__ \ "$date" \ "$numberLong")
      .map(dateTime => Instant.ofEpochMilli(dateTime.toLong).atZone(ZoneOffset.UTC).toLocalDateTime)

  private val localDateTimeWrites: Writes[LocalDateTime] =
    Writes.at[String](__ \ "$date" \ "$numberLong").contramap(_.toInstant(ZoneOffset.UTC).toEpochMilli.toString)

  implicit val dateFormat: Format[LocalDateTime] = Format(localDateTimeReads, localDateTimeWrites)
  implicit val formats: OFormat[DatedCacheMap] = Json.format[DatedCacheMap]

  def apply(cacheMap: CacheMap): DatedCacheMap = DatedCacheMap(cacheMap.id, cacheMap.data)
}
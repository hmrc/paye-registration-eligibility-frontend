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

package test.helpers

import config.AppConfig
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.DefaultReads
import repositories.SessionCacheRepository
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.mongo.cache.DataKey
import uk.gov.hmrc.mongo.test.MongoSupport
import uk.gov.hmrc.mongo.{CurrentTimestampSupport, TimestampSupport}

import scala.concurrent.ExecutionContext

trait SessionHelper extends MongoSupport with BeforeAndAfterEach with DefaultReads {
  self: IntegrationSpecBase =>
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  lazy val repo = new SessionCacheRepository(ts)(ec, appConfig, mongoComponent)
  implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit val hc: HeaderCarrier =
    HeaderCarrier(sessionId = Some(SessionId("test-session-id")))
  val ts: TimestampSupport = new CurrentTimestampSupport

  def sessionCookie: String = "sessionId=test-session-id"

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(repo.deleteFromSession(hc))
    resetWiremock()
  }

  def cacheSessionData(key: String, data: Boolean): Unit = {
    await(repo.putSession[Boolean](DataKey[Boolean](key), data))
  }
}
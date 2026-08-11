/*
 * Copyright 2026 HM Revenue & Customs
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

package connectors

import identifiers.CacheKeys._
import play.api.libs.crypto.DefaultCookieSigner
import repositories.SessionCacheRepository
import test.helpers.{AuthHelper, IntegrationSpecBase, SessionHelper}
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import utils.UserAnswers

class SessionDataCacheConnectorISpec
  extends IntegrationSpecBase
    with SessionHelper
    with AuthHelper {

  override val cookieSigner: DefaultCookieSigner =
    app.injector.instanceOf[DefaultCookieSigner]

  private val connector =
    app.injector.instanceOf[SessionDataCacheConnector]

  private val sessionCacheRepository =
    app.injector.instanceOf[SessionCacheRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
  }

  "saveValueToSession" should {

    "save a value and return updated user answers" in {

      val result =
        await(
          connector.saveValueToSession(
            value = true,
            taxedAwardScheme
          )
        )

      result mustBe Some(
        UserAnswers(
          taxedAwardScheme = Some(true),
          offshoreEmployer = None,
          atLeastOneDirectorHasNino = None
        )
      )

      val savedValue =
        await(
          sessionCacheRepository.getFromSession[Boolean](taxedAwardScheme)
        )

      savedValue mustBe Some(true)
    }
  }

  "fetchUserAnswersFromSession" should {

    "return None when session is empty" in {

      val result =
        await(connector.fetchUserAnswersFromSession)

      result mustBe None
    }

    "return fully populated user answers when all values exist" in {

      await(sessionCacheRepository.putSession(taxedAwardScheme, true))
      await(sessionCacheRepository.putSession(offshoreEmployer, false))
      await(sessionCacheRepository.putSession(atLeastOneDirectorHasNino, true))

      val result =
        await(connector.fetchUserAnswersFromSession)

      result mustBe Some(
        UserAnswers(
          taxedAwardScheme = Some(true),
          offshoreEmployer = Some(false),
          atLeastOneDirectorHasNino = Some(true)
        )
      )
    }

    "return partially populated user answers when only some values exist" in {

      await(sessionCacheRepository.putSession(taxedAwardScheme, true))

      val result =
        await(connector.fetchUserAnswersFromSession)

      result mustBe Some(
        UserAnswers(
          taxedAwardScheme = Some(true),
          offshoreEmployer = None,
          atLeastOneDirectorHasNino = None
        )
      )
    }
  }
}
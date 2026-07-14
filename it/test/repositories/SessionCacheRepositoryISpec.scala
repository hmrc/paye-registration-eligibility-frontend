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

package repositories

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.crypto.DefaultCookieSigner
import test.helpers.{AuthHelper, IntegrationSpecBase, SessionHelper}

class SessionCacheRepositoryISpec
  extends IntegrationSpecBase
    with SessionHelper
    with AuthHelper {

  override implicit lazy val app: Application =
    new GuiceApplicationBuilder()
      .disable[play.filters.csrf.CSRFFilter]
      .configure(additionalConfiguration)
      .build()

  override val cookieSigner: DefaultCookieSigner =
    app.injector.instanceOf[DefaultCookieSigner]

  private val repository =
    app.injector.instanceOf[SessionCacheRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
  }

  "putSession" should {

    "store a value in the session cache" in {

      val result =
        await(
          repository.putSession(
            identifiers.CacheKeys.taxedAwardScheme,
            true
          )
        )

      result mustBe true

      val stored =
        await(
          repository.getFromSession(
            identifiers.CacheKeys.taxedAwardScheme
          )
        )

      stored mustBe Some(true)
    }
  }

  "getFromSession" should {

    "return None when no value exists" in {

      val result =
        await(
          repository.getFromSession(
            identifiers.CacheKeys.taxedAwardScheme
          )
        )

      result mustBe None
    }

    "return a stored value" in {

      await(
        repository.putSession(
          identifiers.CacheKeys.offshoreEmployer,
          false
        )
      )

      val result =
        await(
          repository.getFromSession(
            identifiers.CacheKeys.offshoreEmployer
          )
        )

      result mustBe Some(false)
    }
  }

  "deleteFromSession" should {

    "delete all cached values for the session" in {

      await(
        repository.putSession(
          identifiers.CacheKeys.taxedAwardScheme,
          true
        )
      )

      await(
        repository.putSession(
          identifiers.CacheKeys.offshoreEmployer,
          false
        )
      )

      await(
        repository.deleteFromSession
      )

      await(
        repository.getFromSession(
          identifiers.CacheKeys.taxedAwardScheme
        )
      ) mustBe None

      await(
        repository.getFromSession(
          identifiers.CacheKeys.offshoreEmployer
        )
      ) mustBe None
    }
  }
}
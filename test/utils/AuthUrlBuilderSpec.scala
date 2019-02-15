/*
 * Copyright 2019 HM Revenue & Customs
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

package utils

import base.SpecBase
import config.FrontendAppConfig
import play.api.http.HeaderNames

class AuthUrlBuilderSpec extends SpecBase {

  class Setup {
    val builder = new AuthUrlBuilder {
      override val appConfig: FrontendAppConfig = frontendAppConfig
    }
  }

  "loginCallback" should {
    "be correct" in new Setup {
      val res = builder.redirectToLogin
      res.header.status mustBe 303
      res.header.headers(HeaderNames.LOCATION) mustBe "http://localhost:9025/gg/sign-in?accountType=organisation&continue=http%3A%2F%2Flocalhost%3A9877%2Feligibility-for-paye%2Fauthorised-for-paye&origin=paye-registration-eligibility-frontend"
    }
  }
}

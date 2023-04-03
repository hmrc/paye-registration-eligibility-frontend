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

package www

import helpers.{IntegrationSpecBase, SessionHelper, WiremockHelper}
import play.api.http.HeaderNames
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.crypto.DefaultCookieSigner
import play.api.libs.ws.DefaultWSCookie

class LanguageControllerISpec extends IntegrationSpecBase with SessionHelper with WiremockHelper {

  "setLanguage" should {
    val welshCookie = Some(DefaultWSCookie("PLAY_LANG", "cy", None, Some("/"), None, false, false))
    val englishCookie = Some(DefaultWSCookie("PLAY_LANG", "en", None, Some("/"), None, false, false))

    "return a 303 when language is switched" in {
      val fResponse = buildClient("/defaultLanguage/english")
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie())
        .get()

      val response = await(fResponse)
      response.status mustBe 303
    }

    "return Welsh cookie when language is switched to Welsh" in {
      val fResponse = buildClient("/defaultLanguage/cymraeg")
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie())
        .get()

      val response = await(fResponse)
      response.cookie("PLAY_LANG") mustBe welshCookie
    }

    "return English cookie when language is switched to English" in {
      val fResponse = buildClient("/defaultLanguage/english")
        .withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie())
        .get()

      val response = await(fResponse)
      response.cookie("PLAY_LANG") mustBe englishCookie
    }
  }
}

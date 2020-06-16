/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.tests

import controllers.ControllerSpecBase
import org.scalatest.BeforeAndAfterEach
import play.api.http.HeaderNames
import play.api.test.Helpers._

class FeatureSwitchControllerLanguageSpec extends ControllerSpecBase with BeforeAndAfterEach {


  class Setup {
    val controller = new FeatureSwitchControllerLanguage(messagesControllerComponents){}
  }
      "english/welsh toggle should show on page when feature is true" in new Setup {
        sys.props += (("microservice.services.features.welsh-translation", "false"))
        val before = sys.props.get("microservice.services.features.welsh-translation")
        before mustBe Some("false")
        val result = controller.enableLanguageFunctionality(true)(fakeRequest)
        status(result) mustBe 303
        headers(result).get(HeaderNames.LOCATION).get mustBe controllers.routes.LanguageSwitchController.switchToLanguage("english").url
        await(result)
        val after = sys.props.get("microservice.services.features.welsh-translation")
        after mustBe Some("true")
      }

      "english/welsh toggle should not show on page when feature is false" in new Setup {
        sys.props += (("microservice.services.features.welsh-translation", "true"))
        val before = sys.props.get("microservice.services.features.welsh-translation")
        before mustBe Some("true")
        val result = controller.enableLanguageFunctionality(false)(fakeRequest)
        status(result) mustBe 303
        headers(result).get(HeaderNames.LOCATION).get mustBe controllers.routes.LanguageSwitchController.switchToLanguage("english").url
        await(result)
        val after = sys.props.get("microservice.services.features.welsh-translation")
        after mustBe Some("false")
      }
}

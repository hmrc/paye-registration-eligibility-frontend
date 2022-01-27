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

package controllers

import play.api.test.Helpers._
import views.html.dropout

class IneligibleControllerSpec extends ControllerSpecBase {

  val view: dropout = app.injector.instanceOf[dropout]

  object Controller extends IneligibleController(
    messagesControllerComponents,
    view
  )(frontendAppConfig)

  def viewAsString() = view()(fakeRequest, messages, frontendAppConfig).toString

  "Dropout Controller" must {

    "return OK and the correct view for a GET" in {
      val result = Controller.onPageLoad(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }
  }
}





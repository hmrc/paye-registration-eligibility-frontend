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

package views

import controllers.{RegisterForPayeController, RegisterForPayeControllerImpl}
import org.jsoup.Jsoup
import org.mockito.Mockito._
import play.api.i18n.I18nSupport
import play.api.test.Helpers._
import views.behaviours.ViewBehaviours
import views.html.registerForPaye

class RegisterForPayeViewSpec extends ViewBehaviours with I18nSupport {

  val messageKeyPrefix = "registerForPaye"
  val PAYEThresholdWeeklyAmount = "100"
  def createNewTaxYearView = () => registerForPaye(frontendAppConfig, true, true,PAYEThresholdWeeklyAmount)(fakeRequest, messages)

  def createNormalView = () => registerForPaye(frontendAppConfig, false, true,PAYEThresholdWeeklyAmount)(fakeRequest, messages)

  def createLoggedInView = () => registerForPaye(frontendAppConfig, false, false,PAYEThresholdWeeklyAmount)(fakeRequest, messages)

  class SetupPage {
    reset(mockBusinessRegistrationConnector)
    reset(mockCompanyRegistrationConnector)
    reset(mockAuthUrlBuilder)
    reset(mockAuthConnector)
    val controller = new RegisterForPayeControllerImpl(
      frontendAppConfig,mockAuthConnector,mockAuthUrlBuilder,mockBusinessRegistrationConnector, mockCompanyRegistrationConnector, messagesControllerComponents) {
      override lazy val payeStartUrl = "payeStartURL"
    }
  }

  "RegisterForPaye view" must {
    behave like normalPage(createNewTaxYearView, messageKeyPrefix)

  }

  "Register for PAYE view " must {
    "not display the <signing in to the service> paragraph when logged in" in new SetupPage {

        val result = controller.onPageLoad()(fakeRequest.withSession("authToken" -> "foo"))
        val document = Jsoup.parse(contentAsString(result))

        Option(document.getElementById("signing-in")) mustBe None
      }

    "Display the <signing in to the service> paragraph when not logged in" in new SetupPage {

        val result = controller.onPageLoad()(fakeRequest)
        val document = Jsoup.parse(contentAsString(result))

        document.getElementById("signing-in").text() mustBe "Signing in to the service"
    }
  }
}
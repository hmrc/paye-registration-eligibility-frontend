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

package views

import controllers.RegisterForPayeController
import org.jsoup.Jsoup
import org.mockito.Mockito._
import play.api.test.Helpers._
import utils.TimeMachine
import views.html.registerForPaye
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.global

class RegisterForPayeViewSpec extends ViewSpecBase {

  implicit def ec: ExecutionContext = global

  val messageKeyPrefix = "registerForPaye"
  val PAYEThresholdWeeklyAmount = "100"
  val view: registerForPaye = app.injector.instanceOf[registerForPaye]

  def createNewTaxYearView = () => view(showNewTaxYearContent = true, notLoggedIn = true, PAYEThresholdWeeklyAmount)(fakeRequest(), messages, injectedAppConfig)

  def createNormalView = () => view(showNewTaxYearContent = false, notLoggedIn = true, PAYEThresholdWeeklyAmount)(fakeRequest(), messages, injectedAppConfig)

  def createLoggedInView = () => view(showNewTaxYearContent = false, notLoggedIn = false, PAYEThresholdWeeklyAmount)(fakeRequest(), messages, injectedAppConfig)

  class SetupPage {
    reset(mockBusinessRegistrationConnector)
    reset(mockCompanyRegistrationConnector)
    reset(mockAuthUrlBuilder)
    reset(mockAuthConnector)

    object TestTimeMachine extends TimeMachine(injectedAppConfig)

    val controller = new RegisterForPayeController(
      mockAuthConnector,
      mockAuthUrlBuilder,
      mockBusinessRegistrationConnector,
      mockCompanyRegistrationConnector,
      messagesControllerComponents,
      TestTimeMachine,
      view
    )(injectedAppConfig, ec) {
      override lazy val payeStartUrl = "payeStartURL"
    }
  }

  "Register for PAYE view " must {
    "not display the <signing in to the service> paragraph when logged in" in new SetupPage {

      val result = controller.onPageLoad(fakeRequest().withSession("authToken" -> "foo"))
      val document = Jsoup.parse(contentAsString(result))

      Option(document.getElementById("signing-in")) mustBe None
    }

    "Display the <signing in to the service> paragraph when not logged in" in new SetupPage {

      val result = controller.onPageLoad(fakeRequest())
      val document = Jsoup.parse(contentAsString(result))

      document.getElementById("signing-in").text() mustBe "Signing in to the service"
    }
  }
}
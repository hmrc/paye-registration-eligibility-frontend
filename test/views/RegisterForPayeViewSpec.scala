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

package views

import controllers.RegisterForPayeControllerImpl
import org.jsoup.Jsoup
import org.scalatest.Matchers
import org.scalatest.mockito.MockitoSugar
import play.api.i18n.I18nSupport
import views.behaviours.ViewBehaviours
import views.html.registerForPaye
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.any
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.Future

class RegisterForPayeViewSpec extends ViewBehaviours with I18nSupport with MockitoSugar {

  val messageKeyPrefix = "registerForPaye"

  def createNewTaxYearView = () => registerForPaye(frontendAppConfig, true, true)(fakeRequest, messages)

  def createNormalView = () => registerForPaye(frontendAppConfig, false, true)(fakeRequest, messages)

  def createLoggedInView = () => registerForPaye(frontendAppConfig, false, false)(fakeRequest, messages)

  class SetupPage {
    val controller = new RegisterForPayeControllerImpl(frontendAppConfig, messagesApi) {
      override val payeStartUrl = "payeStartURL"
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

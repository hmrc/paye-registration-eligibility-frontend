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

import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import play.api.mvc.Results
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import utils.TimeMachine
import views.html.registerForPaye

import scala.concurrent.Future

class RegisterForPayeControllerSpec extends ControllerSpecBase {

  val view: registerForPaye = app.injector.instanceOf[registerForPaye]

  object TestTimeMachine extends TimeMachine(injectedAppConfig)

  class Setup {
    reset(mockBusinessRegistrationConnector)
    reset(mockCompanyRegistrationConnector)
    reset(mockAuthUrlBuilder)
    reset(mockAuthConnector)

    object Controller extends RegisterForPayeController(
      mockAuthConnector,
      mockAuthUrlBuilder,
      mockBusinessRegistrationConnector,
      mockCompanyRegistrationConnector,
      messagesControllerComponents,
      TestTimeMachine,
      view
    )(injectedAppConfig) {
      override lazy val payeStartUrl = "payeURL"
      override lazy val otrsUrl = "otrsURL"
    }

    object Controller2 extends RegisterForPayeController(
      mockAuthConnector,
      mockAuthUrlBuilder,
      mockBusinessRegistrationConnector,
      mockCompanyRegistrationConnector,
      messagesControllerComponents,
      TestTimeMachine,
      view
    )(injectedAppConfig)

  }

  implicit val hc = HeaderCarrier()

  val PAYEThresholdWeeklyAmount = TestTimeMachine.getCurrentPayeThreshold

  def viewAsString() = view(TestTimeMachine.isInTaxYearPeriod, true, PAYEThresholdWeeklyAmount)(fakeRequest(), messages, injectedAppConfig).toString

  "onPageLoad" must {

    "return OK and the correct view for a GET" in new Setup {
      val result = Controller.onPageLoad(fakeRequest())

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }
  }
  "onSubmit" must {

    "redirect to sign in (foo) on submit if user not signed in" in new Setup {
      when(mockAuthConnector.authorise[Unit](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.failed(new Exception("")))
      when(mockAuthUrlBuilder.redirectToLogin).thenReturn(Results.SeeOther("foo"))
      val result = Controller.onSubmit(fakeRequest())
      status(result) mustBe SEE_OTHER
      redirectLocation(result).map {
        _.contains("foo") mustBe true
      }
    }
    "redirect to continueToPayeOrOTRS if user signed in" in new Setup {
      when(mockAuthConnector.authorise[Unit](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(()))

      val result = Controller.onSubmit(fakeRequest())
      status(result) mustBe SEE_OTHER
      redirectLocation(result).map {
        _.contains(controllers.routes.RegisterForPayeController.continueToPayeOrOTRS().url) mustBe true
      }
    }
  }


  "continueToPayeOrOTRS" must {
    "redirect to paye reg fe if logged in and ct status / payment ref is present" in new Setup {
      when(mockAuthConnector.authorise[Unit](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(()))
      when(mockBusinessRegistrationConnector.retrieveCurrentProfile(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some("regid")))
      when(mockCompanyRegistrationConnector.getCompanyRegistrationStatusAndPaymentRef(ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful((Some("held"), Some("payment"))))

      val result = Controller.continueToPayeOrOTRS(fakeRequest())
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("payeURL")

    }
    "redirect to otrs if logged in and there is no ct status or pay ref" in new Setup {
      when(mockAuthConnector.authorise[Unit](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(()))
      when(mockBusinessRegistrationConnector.retrieveCurrentProfile(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some("regid")))
      when(mockCompanyRegistrationConnector.getCompanyRegistrationStatusAndPaymentRef(ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful((Option.empty[String], Option.empty[String])))

      val result = Controller.continueToPayeOrOTRS(fakeRequest())
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("otrsURL")

    }
    "redirect to otrs if logged in and ct status is present but no payment ref" in new Setup {
      when(mockAuthConnector.authorise[Unit](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(()))
      when(mockBusinessRegistrationConnector.retrieveCurrentProfile(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some("regid")))
      when(mockCompanyRegistrationConnector.getCompanyRegistrationStatusAndPaymentRef(ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful((Some("held"), Option.empty[String])))

      val result = Controller.continueToPayeOrOTRS(fakeRequest())
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("otrsURL")
    }

    "redirect to otrs if logged in and ct status is not present, i.e they have never started a SCRS journey" in new Setup {
      when(mockAuthConnector.authorise[Unit](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(()))
      when(mockBusinessRegistrationConnector.retrieveCurrentProfile(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Option.empty[String]))

      val result = Controller.continueToPayeOrOTRS(fakeRequest())
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("otrsURL")
    }
    "redirect to index if not logged in" in new Setup {
      when(mockAuthConnector.authorise[Unit](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.failed(new Exception("")))
      val result = Controller.continueToPayeOrOTRS(fakeRequest())
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("/eligibility-for-paye")

    }
  }
  "RegisterForPayeController" should {
    "use the correct paye redirect URL" in new Setup {
      Controller2.payeStartUrl mustBe "http://localhost:9870/register-for-paye/start-pay-as-you-earn"
    }
    "use the correct otrs redirect URL" in new Setup {
      Controller2.otrsUrl mustBe "https://www.tax.service.gov.uk/business-registration/select-taxes"
    }
  }
}
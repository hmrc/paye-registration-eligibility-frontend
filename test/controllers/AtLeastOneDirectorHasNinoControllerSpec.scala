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

package controllers

import controllers.actions.{DataRetrievalAction, SessionAction}
import forms.AtLeastOneDirectorHasNinoFormProvider
import models.requests.{IdentifierRequest, OptionalDataRequest}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.mvc.Results.Redirect
import play.api.mvc._
import play.api.test.Helpers._
import service.SessionDataCacheService
import uk.gov.hmrc.http.HeaderCarrier
import utils.UserAnswers
import views.html.atLeastOneDirectorHasNino

import scala.concurrent.{ExecutionContext, Future}

class AtLeastOneDirectorHasNinoControllerSpec
  extends ControllerSpecBase
    with MockitoSugar {

  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val hc: HeaderCarrier = HeaderCarrier()

  val view: atLeastOneDirectorHasNino =
    app.injector.instanceOf[atLeastOneDirectorHasNino]

  val formProvider = new AtLeastOneDirectorHasNinoFormProvider()
  val form: Form[Boolean] = formProvider()

  val mockService: SessionDataCacheService =
    mock[SessionDataCacheService]

  val mockResult1: Result =
    Redirect("/eligibility-for-paye/offshore-employer")

  val mockResult2: Result =
    Redirect(routes.SessionExpiredController.onPageLoad)

  def controllerWithData(data: Option[Boolean]): AtLeastOneDirectorHasNinoController = {

    val userAnswers =
      UserAnswers(
        taxedAwardScheme = None,
        offshoreEmployer = None,
        atLeastOneDirectorHasNino = data,
      )

    new AtLeastOneDirectorHasNinoController(
      mockService,
      new FakeIdentifierAction,
      new FakeDataRetrievalAction(Some(userAnswers)),
      formProvider,
      messagesControllerComponents,
      view
    )(injectedAppConfig, ec)
  }

  def viewAsString(form: Form[_]): String =
    view(form)(fakeRequest(), messages, injectedAppConfig).toString

  class FakeIdentifierAction
    extends SessionAction(messagesControllerComponents) {

    override val parser: BodyParser[AnyContent] =
      stubBodyParser(AnyContentAsEmpty)

    override def invokeBlock[A](
                                 request: Request[A],
                                 block: IdentifierRequest[A] => Future[Result]
                               ): Future[Result] =
      block(IdentifierRequest(request, "internal-id"))
  }

  class FakeDataRetrievalAction(userAnswers: Option[UserAnswers])
    extends DataRetrievalAction(mockService) {

    override protected def transform[A](
                                         request: IdentifierRequest[A]
                                       ): Future[OptionalDataRequest[A]] =
      Future.successful(
        OptionalDataRequest(
          request.request,
          request.internalId,
          userAnswers
        )
      )
  }

  "AtLeastOneDirectorHasNino Controller" must {

    "return OK and empty view for a GET when no data" in {

      val result =
        controllerWithData(None).onPageLoad()(fakeRequest())

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(form)
    }

    "populate view correctly on GET when previously answered true" in {

      val result =
        controllerWithData(Some(true)).onPageLoad()(fakeRequest())

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(form.fill(true))
    }

    "populate view correctly on GET when previously answered false" in {

      val result =
        controllerWithData(Some(false)).onPageLoad()(fakeRequest())

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(form.fill(false))
    }

    "redirect to next page when valid data is submitted" in {

      when(
        mockService.setAtLeastOneDirectorHasNinoAndRedirectToNextPage(eqTo(true))(
          any[UserAnswers => Call]
        )(any[HeaderCarrier])
      ).thenReturn(Future.successful(mockResult1))

      val request =
        fakeRequest("POST")
          .withFormUrlEncodedBody("value" -> "true")

      val result =
        controllerWithData(None).onSubmit()(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe
        Some(routes.OffshoreEmployerController.onPageLoad.url)
    }

    "return Bad Request when invalid data is submitted" in {

      val request =
        fakeRequest("POST")
          .withFormUrlEncodedBody("value" -> "invalid")

      val result =
        controllerWithData(None).onSubmit()(request)

      status(result) mustBe BAD_REQUEST
    }

    "redirect to Session Expired when service returns session expired redirect" in {

      when(
        mockService.setAtLeastOneDirectorHasNinoAndRedirectToNextPage(eqTo(true))(
          any[UserAnswers => Call]
        )(any[HeaderCarrier])
      ).thenReturn(Future.successful(mockResult2))

      val request =
        fakeRequest("POST")
          .withFormUrlEncodedBody("value" -> "true")

      val result =
        controllerWithData(None).onSubmit()(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe
        Some(routes.SessionExpiredController.onPageLoad.url)
    }
  }
}

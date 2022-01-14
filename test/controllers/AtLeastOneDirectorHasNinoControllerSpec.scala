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

import connectors.DataCacheConnector
import controllers.actions._
import forms.AtLeastOneDirectorHasNinoFormProvider
import identifiers.AtLeastOneDirectorHasNinoId
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.{when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json.JsBoolean
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import views.html.atLeastOneDirectorHasNino

import scala.concurrent.Future

class AtLeastOneDirectorHasNinoControllerSpec extends ControllerSpecBase {

  def onwardRoute = routes.IndexController.onPageLoad

  val view = app.injector.instanceOf[atLeastOneDirectorHasNino]

  val formProvider = new AtLeastOneDirectorHasNinoFormProvider()
  val form = formProvider()

  val mockDataCacheConnector = mock[DataCacheConnector]

  object Controller extends AtLeastOneDirectorHasNinoController(
    frontendAppConfig,
    mockDataCacheConnector,
    new FakeAuthAction(messagesControllerComponents),
    getEmptyCacheMap,
    formProvider, messagesControllerComponents,
    view
  )

  def viewAsString(form: Form[_] = form) = view(frontendAppConfig, form)(fakeRequest, messages).toString

  "AtLeastOneDirectorHasNino Controller" must {

    "return OK and the correct view for a GET" in {
      val result = Controller.onPageLoad(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val validData = Map(AtLeastOneDirectorHasNinoId.toString -> JsBoolean(true))
      val getRelevantData = new FakeDataRetrievalAction(Some(CacheMap(cacheMapId, validData)), messagesControllerComponents, sessionRepository, cascadeUpsert)

      object Controller extends AtLeastOneDirectorHasNinoController(
        frontendAppConfig,
        mockDataCacheConnector,
        new FakeAuthAction(messagesControllerComponents),
        getRelevantData,
        formProvider, messagesControllerComponents,
        view
      )


      val result = Controller.onPageLoad()(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(true))
    }

    "redirect to the Offshore Employers page if yes is selected" in {
      val validData = Map(AtLeastOneDirectorHasNinoId.toString -> JsBoolean(true))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      when(mockDataCacheConnector.save(any(), any(), any())(any()))
        .thenReturn(Future.successful(CacheMap(cacheMapId, validData)))

      val result = Controller.onSubmit(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.OffshoreEmployerController.onPageLoad.url)
    }

    "redirect to the Dropout page if no is selected" in {
      val validData = Map(AtLeastOneDirectorHasNinoId.toString -> JsBoolean(false))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "false"))

      when(mockDataCacheConnector.save(any(), any(), any())(any()))
        .thenReturn(Future.successful(CacheMap(cacheMapId, validData)))

      val result = Controller.onSubmit(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.IneligibleController.onPageLoad.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = Controller.onSubmit(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }
  }
}

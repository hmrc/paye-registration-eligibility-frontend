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

import connectors.{DataCacheConnector, FakeDataCacheConnector}
import controllers.actions._
import forms.TaxedAwardSchemeFormProvider
import identifiers.TaxedAwardSchemeId
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.data.Form
import play.api.libs.json.JsBoolean
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import views.html.taxedAwardScheme

import scala.concurrent.Future

class TaxedAwardSchemeControllerSpec extends ControllerSpecBase {

  def onwardRoute = routes.IndexController.onPageLoad

  val formProvider = new TaxedAwardSchemeFormProvider()
  val form = formProvider()

  val view: taxedAwardScheme = app.injector.instanceOf[taxedAwardScheme]

  val mockDataCacheConnector = mock[DataCacheConnector]

  val fakeDataCacheConnector = new FakeDataCacheConnector(sessionRepository, cascadeUpsert)

  object Controller extends TaxedAwardSchemeController(
    fakeDataCacheConnector,
    new FakeAuthAction(messagesControllerComponents),
    getEmptyCacheMap,
    new DataRequiredAction(messagesControllerComponents),
    formProvider,
    messagesControllerComponents,
    view
  )(injectedAppConfig)

  def viewAsString(form: Form[_] = form) = view(form)(fakeRequest, messages, injectedAppConfig).toString

  "TaxedAwardScheme Controller" must {

    "return OK and the correct view for a GET" in {
      val result = Controller.onPageLoad(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val validData = Map(TaxedAwardSchemeId.toString -> JsBoolean(true))
      val getRelevantData = new FakeDataRetrievalAction(Some(CacheMap(cacheMapId, validData)), messagesControllerComponents, sessionRepository, cascadeUpsert)

      object Controller extends TaxedAwardSchemeController(
        fakeDataCacheConnector,
        new FakeAuthAction(messagesControllerComponents),
        getRelevantData,
        new DataRequiredAction(messagesControllerComponents),
        formProvider,
        messagesControllerComponents,
        view
      )(injectedAppConfig)

      val result = Controller.onPageLoad(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(true))
    }

    "redirect to the dropout page if yes is selected" in {
      val validData = Map(TaxedAwardSchemeId.toString -> JsBoolean(true))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      object Controller extends TaxedAwardSchemeController(
        mockDataCacheConnector,
        new FakeAuthAction(messagesControllerComponents),
        new FakeDataRetrievalAction(Some(CacheMap(cacheMapId, validData)), messagesControllerComponents, sessionRepository, cascadeUpsert),
        new DataRequiredAction(messagesControllerComponents),
        formProvider,
        messagesControllerComponents,
        view
      )(injectedAppConfig)

      when(mockDataCacheConnector.save(any(), any(), any())(any()))
        .thenReturn(Future.successful(CacheMap(cacheMapId, validData)))

      val result = Controller.onSubmit(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.IneligibleController.onPageLoad.url)
    }

    "redirect to the you must register page if no is selected" in {
      val validData = Map(TaxedAwardSchemeId.toString -> JsBoolean(false))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "false"))

      object Controller extends TaxedAwardSchemeController(
        mockDataCacheConnector,
        new FakeAuthAction(messagesControllerComponents),
        new FakeDataRetrievalAction(Some(CacheMap(cacheMapId, validData)), messagesControllerComponents, sessionRepository, cascadeUpsert),
        new DataRequiredAction(messagesControllerComponents),
        formProvider,
        messagesControllerComponents,
        view
      )(injectedAppConfig)

      when(mockDataCacheConnector.save(any(), any(), any())(any()))
        .thenReturn(Future.successful(CacheMap(cacheMapId, validData)))

      val result = Controller.onSubmit(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.RegisterForPayeController.onPageLoad.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = Controller.onSubmit(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to IndexController for a GET if no existing data is found" in {
      object Controller extends TaxedAwardSchemeController(
        fakeDataCacheConnector,
        new FakeAuthAction(messagesControllerComponents),
        new FakeDataRetrievalAction(None, messagesControllerComponents, sessionRepository, cascadeUpsert),
        new DataRequiredAction(messagesControllerComponents),
        formProvider,
        messagesControllerComponents,
        view
      )(injectedAppConfig)

      val result = Controller.onPageLoad(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.IndexController.onPageLoad.url)
    }

    "redirect to IndexController for a POST if no existing data is found" in {
      object Controller extends TaxedAwardSchemeController(
        fakeDataCacheConnector,
        new FakeAuthAction(messagesControllerComponents),
        new FakeDataRetrievalAction(None, messagesControllerComponents, sessionRepository, cascadeUpsert),
        new DataRequiredAction(messagesControllerComponents),
        formProvider,
        messagesControllerComponents,
        view
      )(injectedAppConfig)

      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
      val result = Controller.onSubmit(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.IndexController.onPageLoad.url)
    }
  }
}

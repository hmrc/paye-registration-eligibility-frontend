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

package service

import base.SpecBase
import connectors.SessionDataCacheConnector
import controllers.routes
import identifiers.CacheKeys._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{Call, Result}
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.cache.DataKey
import utils.UserAnswers

import scala.concurrent.{ExecutionContext, Future}

class SessionDataCacheServiceSpec extends SpecBase with MockitoSugar with ScalaFutures {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  private val mockConnector = mock[SessionDataCacheConnector]

  private val service =
    new SessionDataCacheService(mockConnector)(ec)

  private val userAnswers = mock[UserAnswers]

  private val redirectToNextPage: UserAnswers => Call = mock[UserAnswers => Call]

  private val expectedCall = Call("GET", "/next-page")

  private def successfulSaveTest(
                                  method: (Boolean) => (UserAnswers => Call) => Future[Result],
                                  key: DataKey[Boolean]
                                ): Unit = {

    when(
      mockConnector.saveValueToSession(
        value = true,
        key
      )(hc, ec)
    ).thenReturn(Future.successful(Some(userAnswers)))

    when(
      redirectToNextPage(userAnswers)
    ).thenReturn(expectedCall)

    val result =
      method(true)(redirectToNextPage)

    status(result) mustBe SEE_OTHER
    redirectLocation(result) mustBe Some("/next-page")

    verify(mockConnector).saveValueToSession(
      value = true,
      key
    )(hc, ec)
  }

  private def sessionExpiredTest(
                                  method: (Boolean) => (UserAnswers => Call) => Future[Result],
                                  key: DataKey[Boolean]
                                ): Unit = {

    when(
      mockConnector.saveValueToSession(
        value = true,
        key
      )(hc, ec)
    ).thenReturn(Future.successful(None))

    val result =
      method(true)(redirectToNextPage)

    status(result) mustBe SEE_OTHER
    redirectLocation(result) mustBe
      Some(routes.SessionExpiredController.onPageLoad.url)
  }

  "setTaxedAwardSchemeAndRedirectToNextPage" should {

    "redirect to next page when answers are successfully saved" in {
      successfulSaveTest(
        service.setTaxedAwardSchemeAndRedirectToNextPage,
        taxedAwardScheme
      )
    }

    "redirect to session expired page when no user answers are returned" in {
      sessionExpiredTest(
        service.setTaxedAwardSchemeAndRedirectToNextPage,
        taxedAwardScheme
      )
    }
  }

  "setOffshoreEmployerAndRedirectToNextPage" should {

    "redirect to next page when answers are successfully saved" in {
      successfulSaveTest(
        service.setOffshoreEmployerAndRedirectToNextPage,
        offshoreEmployer
      )
    }

    "redirect to session expired page when no user answers are returned" in {
      sessionExpiredTest(
        service.setOffshoreEmployerAndRedirectToNextPage,
        offshoreEmployer
      )
    }
  }

  "setAtLeastOneDirectorHasNinoAndRedirectToNextPage" should {

    "redirect to next page when answers are successfully saved" in {
      successfulSaveTest(
        service.setAtLeastOneDirectorHasNinoAndRedirectToNextPage,
        atLeastOneDirectorHasNino
      )
    }

    "redirect to session expired page when no user answers are returned" in {
      sessionExpiredTest(
        service.setAtLeastOneDirectorHasNinoAndRedirectToNextPage,
        atLeastOneDirectorHasNino
      )
    }
  }

  "getUserAnswers" should {

    "return user answers from connector" in {

      when(
        mockConnector.fetchUserAnswersFromSession(any(), any())
      ).thenReturn(Future.successful(Some(userAnswers)))

      val result =
        service.getUserAnswers.futureValue

      result mustBe Some(userAnswers)
    }

    "return None when no user answers exist" in {

      when(
        mockConnector.fetchUserAnswersFromSession(any(), any())
      ).thenReturn(Future.successful(None))

      val result =
        service.getUserAnswers.futureValue

      result mustBe None
    }
  }
}
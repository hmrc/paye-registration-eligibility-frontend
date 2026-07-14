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

package controllers.actions

import base.SpecBase
import models.requests.{IdentifierRequest, OptionalDataRequest}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import service.SessionDataCacheService
import uk.gov.hmrc.http.HeaderCarrier
import utils.UserAnswers

import scala.concurrent.{ExecutionContext, Future}

class DataRetrievalActionSpec
  extends SpecBase
    with MockitoSugar
    with ScalaFutures {

  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val sessionDataCacheService = mock[SessionDataCacheService]

  def mockGetAnswers(result: Option[UserAnswers]): OngoingStubbing[Future[Option[UserAnswers]]] =
    when(sessionDataCacheService.getUserAnswers(any[HeaderCarrier]))
      .thenReturn(Future.successful(result))

  class Harness(mockSessionService: SessionDataCacheService) extends
    DataRetrievalAction(mockSessionService) {
    def callTransform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] = transform(request)
  }

  "DataRetrievalAction" should {

    "add Some(userAnswers) when cache has data" in {

      val userAnswers = UserAnswers(Some(true), None, None)
      val action = new Harness(sessionDataCacheService)
      mockGetAnswers(Some(userAnswers))

      val identifierRequest =
        IdentifierRequest(FakeRequest(), "test-id")

      whenReady(action.callTransform(identifierRequest)) { result =>
        result.userAnswers mustBe Some(userAnswers)
      }
    }

    "add None when cache is empty" in {
      mockGetAnswers(None)
      val action = new Harness(sessionDataCacheService)

      val identifierRequest =
        IdentifierRequest(FakeRequest(), "test-id")

      whenReady(action.callTransform(identifierRequest)) { result =>
        result.userAnswers mustBe None
      }
    }
  }
}
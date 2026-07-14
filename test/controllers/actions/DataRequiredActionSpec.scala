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
import controllers.routes
import models.requests.{DataRequest, OptionalDataRequest}
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.UserAnswers

import scala.concurrent.{ExecutionContext, Future}

class DataRequiredActionSpec
  extends SpecBase
    with MockitoSugar
    with ScalaFutures {

  implicit val ec: ExecutionContext = ExecutionContext.global
  private val action = new TestDataRequiredAction

  private class TestDataRequiredAction
    extends DataRequiredAction {

    def testRefine[A](
                       request: OptionalDataRequest[A]
                     ): Future[Either[Result, DataRequest[A]]] =
      refine(request)
  }

  "DataRequiredAction" should {

    "return DataRequest when user answers exist" in {

      val userAnswers =
        UserAnswers(
          taxedAwardScheme = Some(true),
          offshoreEmployer = Some(false),
          atLeastOneDirectorHasNino = Some(true)
        )

      val request =
        OptionalDataRequest(
          FakeRequest(),
          "internal-id",
          Some(userAnswers)
        )

      val result =
        action.testRefine(request).futureValue

      result mustBe Right(
        DataRequest(
          request.request,
          request.internalId,
          userAnswers
        )
      )
    }


    "redirect to IndexController when user answers are missing" in {

      val request =
        OptionalDataRequest(
          FakeRequest(),
          "internal-id",
          None
        )

      val result =
        action.testRefine(request).futureValue

      result.isLeft mustBe true

      val redirectResult =
        result.swap.getOrElse(fail("Expected Left but got Right"))

      status(Future.successful(redirectResult)) mustBe SEE_OTHER

      redirectLocation(Future.successful(redirectResult)) mustBe
        Some(routes.IndexController.onPageLoad.url)
    }
  }
}
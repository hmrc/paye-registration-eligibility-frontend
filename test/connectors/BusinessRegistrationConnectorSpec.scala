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

package connectors

import base.SpecBase
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.concurrent.Eventually
import play.api.Logger
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.test.LogCapturing

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BusinessRegistrationConnectorSpec extends SpecBase with LogCapturing with Eventually {

  class Setup {
    reset(mockHttpClient)
    val businessRegistrationConnector = new BusinessRegistrationConnector {
      override val businessRegUrl: String = "foo"
      override val wSHttp: CoreGet = mockHttpClient
    }

    implicit val hc = HeaderCarrier()
  }

  "retrieveCurrentProfile" should {
    val BusRegJson = Json.parse(
      s"""
         |{
         | "registrationID" : "regId",
         | "language" : "ENG",
         | "formCreationTimestamp" : "2019-02-07T13:48:05Z"
         |}
      """.stripMargin)

    val BusRegJsonNoRegId = Json.parse(
      s"""
         |{
         | "language" : "ENG",
         | "formCreationTimestamp" : "2019-02-07T13:48:05Z"
         |}
      """.stripMargin)

    "return some(regid) when 200 returned with json body containing a registration id" in new Setup {
      when(mockHttpClient.GET[HttpResponse](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any[HeaderCarrier](), ArgumentMatchers.any()))
        .thenReturn(Future(HttpResponse(responseStatus = 200, responseJson = Some(BusRegJson))))

      await(businessRegistrationConnector.retrieveCurrentProfile) mustBe Some("regId")
    }

    "return None when 202 returned with no json body and log it" in new Setup {
      when(mockHttpClient.GET[HttpResponse](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any[HeaderCarrier](), ArgumentMatchers.any()))
        .thenReturn(Future(HttpResponse(responseStatus = 202, responseJson = None)))


      withCaptureOfLoggingFrom(Logger) { logEvents =>
        await(businessRegistrationConnector.retrieveCurrentProfile) mustBe None
        logEvents.map(_.getMessage) mustBe List("[BusinessRegistrationConnector] [retrieveCurrentProfile] status not 200, actually 202 user directed to OTRS")
        logEvents.size mustBe 1
      }
    }

    "return None if 500 status is returned (Problem calling BR)" in new Setup {
      when(mockHttpClient.GET[HttpResponse](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any[HeaderCarrier](), ArgumentMatchers.any()))
        .thenReturn(Future.failed(new HttpException("foo", 500)))

      await(businessRegistrationConnector.retrieveCurrentProfile) mustBe None
    }

    "return None if 404 status is returned (None SCRS user)" in new Setup {
      when(mockHttpClient.GET[HttpResponse](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any[HeaderCarrier](), ArgumentMatchers.any()))
        .thenReturn(Future.failed(new NotFoundException("foo")))

      await(businessRegistrationConnector.retrieveCurrentProfile) mustBe None
    }

    "return None if no regid is in json (Exception)" in new Setup {
      when(mockHttpClient.GET[HttpResponse](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any[HeaderCarrier](), ArgumentMatchers.any()))
        .thenReturn(Future(HttpResponse(responseStatus = 200, responseJson = Some(BusRegJsonNoRegId))))

      await(businessRegistrationConnector.retrieveCurrentProfile) mustBe None
    }
  }

}

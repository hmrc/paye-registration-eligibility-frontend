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

package connectors

import base.SpecBase
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.concurrent.Eventually
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HttpClient, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BusinessRegistrationConnectorSpec extends SpecBase with Eventually {

  class Setup {
    reset(mockHttpClient)
    val businessRegistrationConnector = new BusinessRegistrationConnector(injectedAppConfig, mockHttpClient) {
      override lazy val businessRegUrl: String = "foo"
      override val wSHttp: HttpClient = mockHttpClient
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
      when(mockHttpClient.GET[HttpResponse](ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any[HeaderCarrier](), ArgumentMatchers.any()))
        .thenReturn(Future(HttpResponse(200, json = BusRegJson, Map())))

      await(businessRegistrationConnector.retrieveCurrentProfile) mustBe Some("regId")
    }

    "return None when 202 returned with no json body and log it" in new Setup {
      when(mockHttpClient.GET[HttpResponse](ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any[HeaderCarrier](), ArgumentMatchers.any()))
        .thenReturn(Future(HttpResponse(202, "")))

      await(businessRegistrationConnector.retrieveCurrentProfile) mustBe None
    }

    "return None if 500 status is returned (Problem calling BR)" in new Setup {
      when(mockHttpClient.GET[HttpResponse](ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any[HeaderCarrier](), ArgumentMatchers.any()))
        .thenReturn(Future.failed(new HttpException("foo", responseCode = 500)))

      await(businessRegistrationConnector.retrieveCurrentProfile) mustBe None
    }

    "return None if 404 status is returned (None SCRS user)" in new Setup {
      when(mockHttpClient.GET[HttpResponse](ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any[HeaderCarrier](), ArgumentMatchers.any()))
        .thenReturn(Future.failed(new NotFoundException("foo")))

      await(businessRegistrationConnector.retrieveCurrentProfile) mustBe None
    }

    "return None if no regid is in json (Exception)" in new Setup {
      when(mockHttpClient.GET[HttpResponse](ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any[HeaderCarrier](), ArgumentMatchers.any()))
        .thenReturn(Future(HttpResponse(200, json = BusRegJsonNoRegId, Map())))

      await(businessRegistrationConnector.retrieveCurrentProfile) mustBe None
    }
  }

}

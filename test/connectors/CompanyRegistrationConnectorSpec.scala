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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.http.{BadRequestException, CoreGet, HeaderCarrier}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CompanyRegistrationConnectorSpec extends SpecBase {

  val testUrl = "testUrl"
  val testUri = "testUri"

  class Setup(stubbed: Boolean) {
    val testConnector = new CompanyRegistrationConnector {
      val companyRegistrationUri          = testUri
      val companyRegistrationUrl          = testUrl
      val stubUri                         = testUri
      val stubUrl                         = testUrl
      override val http: CoreGet          = mockHttpClient
      override val featureSwitch          = mockFeatureSwitch
      override def useCompanyRegistration = stubbed
    }
    implicit val hc = HeaderCarrier()
  }

  val status = "submitted"
  val transactionId = "tx-id"
  val ackRefStatus = "04"
  val ackRefStatusOpt = Some(ackRefStatus)

  val profileJson =
    Json.parse(
      s"""
         |{
         |    "registration-id" : "testRegId",
         |    "status" : "$status",
         |    "confirmationReferences" : {
         |       "acknowledgement-reference" : "BRCT-0123456789",
         |       "transaction-id" : "$transactionId"
         |    },
         |    "acknowledgementReferences" : {
         |       "status" : "$ackRefStatus"
         |    }
         |}
      """.stripMargin).as[JsObject]

  val profileJsonNoStatus =
    Json.parse(
      s"""
         |{
         |    "registration-id" : "testRegId"
         |}
      """.stripMargin).as[JsObject]

  "getCompanyRegistrationDetails" should {
    "return the status contained in the CT doc" in new Setup(false) {
      when(mockHttpClient.GET[JsObject](any())(any(), any[HeaderCarrier](), any()))
        .thenReturn(Future(profileJson))

      val result = await(testConnector.getCompanyRegistrationStatusAndPaymentRef("testRegId"))
      result mustBe ((Some("submitted"), Option.empty[String]))
    }
  "return a none is JSON returned with no status element in" in new Setup(false) {
      when(mockHttpClient.GET[JsObject](any())(any(), any[HeaderCarrier](), any()))
        .thenReturn(Future(profileJsonNoStatus))

      val result = await(testConnector.getCompanyRegistrationStatusAndPaymentRef("testRegId"))
      result mustBe ((Option.empty[String], Option.empty[String]))
    }

    "throw nothing if exception was thrown" in new Setup(false) {
      when(mockHttpClient.GET[JsObject](any())(any(), any[HeaderCarrier](), any()))
        .thenReturn(Future.failed(new BadRequestException("tstException")))

      val result = await(testConnector.getCompanyRegistrationStatusAndPaymentRef("testRegId"))
      result mustBe ((Option.empty[String],Option.empty[String]))

    }



  }

}
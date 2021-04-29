/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.test.Helpers._

class CompanyRegistrationConnectorSpec extends SpecBase {

  val testUrl = "testUrl"
  val testUri = "testUri"

  class Setup(stubbed: Boolean) {
    val testConnector = new CompanyRegistrationConnector(mockFeatureSwitch, mockHttpClient, frontendAppConfig) {
      override lazy val companyRegistrationUri: String = testUri
      override lazy val companyRegistrationUrl: String = testUrl
      override lazy val stubUri: String = testUri
      override lazy val stubUrl: String = testUrl
      override def useCompanyRegistration: Boolean = stubbed
    }
    implicit val hc: HeaderCarrier = HeaderCarrier()
  }

  val status = "submitted"
  val transactionId = "tx-id"
  val ackRefStatus = "04"
  val ackRefStatusOpt = Some(ackRefStatus)

  val profileJson: JsObject =
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

  val profileJsonNoStatus: JsObject =
    Json.parse(
      s"""
         |{
         |    "registration-id" : "testRegId"
         |}
      """.stripMargin).as[JsObject]

  "getCompanyRegistrationDetails" should {
    "return the status contained in the CT doc" in new Setup(false) {
      when(mockHttpClient.GET[JsObject](any(),any(),any())(any(), any[HeaderCarrier](), any()))
        .thenReturn(Future(profileJson))

      val result: (Option[String], Option[String]) = await(testConnector.getCompanyRegistrationStatusAndPaymentRef("testRegId"))
      result mustBe ((Some("submitted"), Option.empty[String]))
    }
    "return a none is JSON returned with no status element in" in new Setup(false) {
      when(mockHttpClient.GET[JsObject](any(),any(),any())(any(), any[HeaderCarrier](), any()))
        .thenReturn(Future(profileJsonNoStatus))

      val result: (Option[String], Option[String])= await(testConnector.getCompanyRegistrationStatusAndPaymentRef("testRegId"))
      result mustBe ((Option.empty[String], Option.empty[String]))
    }

    "throw nothing if exception was thrown" in new Setup(false) {
      when(mockHttpClient.GET[JsObject](any(),any(),any())(any(), any[HeaderCarrier](), any()))
        .thenReturn(Future.failed(new BadRequestException("tstException")))

      val result: (Option[String], Option[String]) = await(testConnector.getCompanyRegistrationStatusAndPaymentRef("testRegId"))
      result mustBe ((Option.empty[String], Option.empty[String]))

    }


  }

}
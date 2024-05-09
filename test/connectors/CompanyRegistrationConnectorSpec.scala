/*
 * Copyright 2023 HM Revenue & Customs
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
import ch.qos.logback.classic.Level
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier}
import utils.LogCapturingHelper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CompanyRegistrationConnectorSpec extends SpecBase with LogCapturingHelper {

  val testUrl = "testUrl"
  val testUri = "testUri"

  class Setup(stubbed: Boolean) {
    object TestCompanyRegistrationConnector extends CompanyRegistrationConnector(mockFeatureSwitch, mockHttpClient, injectedAppConfig) {
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
  val ackRefStatusOpt: Option[String] = Some(ackRefStatus)

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

    "return the response from the HttpReads when successful" in new Setup(false) {
      when(mockHttpClient.GET[(Option[String], Option[String])](any(), any(), any())(any(), any[HeaderCarrier](), any()))
        .thenReturn(Future.successful(Some(status) -> None))

      val result: (Option[String], Option[String]) = await(TestCompanyRegistrationConnector.getCompanyRegistrationStatusAndPaymentRef("testRegId"))
      result mustBe Some(status) -> None
    }

    "return None if exception was thrown but log an error" in new Setup(false) {
      when(mockHttpClient.GET[(Option[String], Option[String])](any(), any(), any())(any(), any[HeaderCarrier](), any()))
        .thenReturn(Future.failed(new BadRequestException("tstException")))

      withCaptureOfLoggingFrom(TestCompanyRegistrationConnector.logger) { logs =>
        val result: (Option[String], Option[String]) = await(TestCompanyRegistrationConnector.getCompanyRegistrationStatusAndPaymentRef("testRegId"))
        result mustBe None -> None

        logs.containsMsg(Level.ERROR, "[TestCompanyRegistrationConnector][getCompanyRegistrationStatusAndPaymentRef] tstException")
      }
    }
  }
}

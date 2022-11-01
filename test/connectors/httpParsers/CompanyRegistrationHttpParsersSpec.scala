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

package connectors.httpParsers

import base.SpecBase
import ch.qos.logback.classic.Level
import org.scalatest.concurrent.Eventually
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.http._
import utils.LogCapturingHelper

class CompanyRegistrationHttpParsersSpec extends SpecBase with Eventually with LogCapturingHelper {

  val status = "submitted"
  val paytReference = "payt1234"

  val profileJson: JsObject =
    Json.parse(
      s"""
         |{
         |    "registration-id" : "testRegId",
         |    "status" : "$status"
         |}
      """.stripMargin).as[JsObject]

  val profileWithPaymentRefJson: JsObject =
    Json.parse(
      s"""
         |{
         |    "registration-id" : "testRegId",
         |    "status" : "$status",
         |    "confirmationReferences" : {
         |       "payment-reference" : "$paytReference"
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

  "calling .companyRegistrationStatusAndPaymentRefHttpParser" should {

    "return (Some(status), None) when OK is returned with valid JSON but only status is present" in {

      val response = HttpResponse(OK, json = profileJson, Map())

      CompanyRegistrationHttpParsers.companyRegistrationStatusAndPaymentRefHttpParser.read("", "", response) mustBe Some(status) -> None
    }

    "return (Some(status), Some(paytReference) when OK is returned with valid JSON with Payment Reference" in {

      val response = HttpResponse(OK, json = profileWithPaymentRefJson, Map())

      CompanyRegistrationHttpParsers.companyRegistrationStatusAndPaymentRefHttpParser.read("", "", response) mustBe Some(status) -> Some(paytReference)
    }

    "return (None, None) when OK is returned but no status exists (log an error)" in {

      val response = HttpResponse(OK, json = profileJsonNoStatus, Map())

      withCaptureOfLoggingFrom(CompanyRegistrationHttpParsers.logger) { logs =>
        CompanyRegistrationHttpParsers.companyRegistrationStatusAndPaymentRefHttpParser.read("", "", response) mustBe None -> None
        logs.containsMsg(Level.ERROR, "[CompanyRegistrationHttpParsers][companyRegistrationStatusAndPaymentRefHttpParser] json returned from CR does not contain status, user will redirect to OTRS")
      }
    }

    "return None when NOT_FOUND is returned" in {

      val response = HttpResponse(NOT_FOUND, "")
      CompanyRegistrationHttpParsers.companyRegistrationStatusAndPaymentRefHttpParser.read("", "", response) mustBe None -> None
    }

    "return None when any other status is returned and log an error" in {

      val response = HttpResponse(INTERNAL_SERVER_ERROR, "")

      withCaptureOfLoggingFrom(CompanyRegistrationHttpParsers.logger) { logs =>
        CompanyRegistrationHttpParsers.companyRegistrationStatusAndPaymentRefHttpParser.read("", "", response) mustBe None -> None
        logs.containsMsg(Level.ERROR, s"[CompanyRegistrationHttpParsers][companyRegistrationStatusAndPaymentRefHttpParser] status not 200, actually $INTERNAL_SERVER_ERROR user directed to OTRS")
      }
    }
  }
}

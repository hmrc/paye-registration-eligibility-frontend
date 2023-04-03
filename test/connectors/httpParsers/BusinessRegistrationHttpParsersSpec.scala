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

package connectors.httpParsers

import base.SpecBase
import ch.qos.logback.classic.Level
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.concurrent.Eventually
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HttpClient, _}
import utils.LogCapturingHelper

import scala.concurrent.Future

class BusinessRegistrationHttpParsersSpec extends SpecBase with Eventually with LogCapturingHelper {

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

  "calling .retrieveCurrentProfileHttpReads" should {

    "return Some(regid) when OK is returned with valid JSON" in {

      val response = HttpResponse(OK, json = BusRegJson, Map())

      BusinessRegistrationHttpParsers.retrieveCurrentProfileHttpReads.read("", "", response) mustBe Some("regId")
    }

    "return None when OK is returned but not registrationId exists (log an error)" in {

      val response = HttpResponse(OK, json = BusRegJsonNoRegId, Map())

      withCaptureOfLoggingFrom(BusinessRegistrationHttpParsers.logger) { logs =>
        BusinessRegistrationHttpParsers.retrieveCurrentProfileHttpReads.read("", "", response) mustBe None
        logs.containsMsg(Level.ERROR, "[BusinessRegistrationHttpParsers][retrieveCurrentProfileHttpReads] json returned from BR does not contain registrationID, user will redirect to OTRS")
      }
    }

    "return None when NOT_FOUND is returned" in {

      val response = HttpResponse(NOT_FOUND, "")
      BusinessRegistrationHttpParsers.retrieveCurrentProfileHttpReads.read("", "", response) mustBe None
    }

    "return None when any other status is returned and log an error" in {

      val response = HttpResponse(INTERNAL_SERVER_ERROR, "")

      withCaptureOfLoggingFrom(BusinessRegistrationHttpParsers.logger) { logs =>
        BusinessRegistrationHttpParsers.retrieveCurrentProfileHttpReads.read("", "", response) mustBe None
        logs.containsMsg(Level.ERROR, s"[BusinessRegistrationHttpParsers][retrieveCurrentProfileHttpReads] status not 200, actually $INTERNAL_SERVER_ERROR user directed to OTRS")
      }
    }
  }
}

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
import org.mockito.Mockito._
import org.scalatest.concurrent.Eventually
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HttpClient, _}
import utils.LogCapturingHelper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BusinessRegistrationConnectorSpec extends SpecBase with Eventually with LogCapturingHelper {

  class Setup {
    object TestBusinessRegistrationConnector extends BusinessRegistrationConnector(injectedAppConfig, mockHttpClient) {
      override lazy val businessRegUrl: String = "foo"
      override val wSHttp: HttpClient = mockHttpClient
    }

    implicit val hc = HeaderCarrier()
  }

  "calling .retrieveCurrentProfile" should {

    "return Some(regid) is returned from the HttpReads" in new Setup {
      when(mockHttpClient.GET[Option[String]](any(), any(), any())(any(), any[HeaderCarrier](), any()))
        .thenReturn(Future.successful(Some("regId")))

      await(TestBusinessRegistrationConnector.retrieveCurrentProfile) mustBe Some("regId")
    }

    "return None is returned from the HttpReads" in new Setup {
      when(mockHttpClient.GET[Option[String]](any(), any(), any())(any(), any[HeaderCarrier](), any()))
        .thenReturn(Future.successful(None))

      await(TestBusinessRegistrationConnector.retrieveCurrentProfile) mustBe None
    }

    "return None if future unexpectedly fails with exception and log an error" in new Setup {
      when(mockHttpClient.GET[HttpResponse](any(), any(), any())(any(), any[HeaderCarrier](), any()))
        .thenReturn(Future.failed(new Exception("bang")))

      withCaptureOfLoggingFrom(TestBusinessRegistrationConnector.logger) { logs =>
        await(TestBusinessRegistrationConnector.retrieveCurrentProfile) mustBe None
        logs.containsMsg(Level.ERROR, s"[TestBusinessRegistrationConnector][retrieveCurrentProfile] exception returned 'bang' user directed to OTRS")
      }
    }
  }

}

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

import config.AppConfig
import connectors.httpParsers.BusinessRegistrationHttpParsers
import uk.gov.hmrc.http.{HttpClient, _}
import utils.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessRegistrationConnector @Inject()(val appConfig: AppConfig,
                                              val wSHttp: HttpClient) extends Logging with BusinessRegistrationHttpParsers {

  lazy val businessRegUrl: String = appConfig.config.baseUrl("business-registration")

  def retrieveCurrentProfile(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[String]] = {

    wSHttp.GET[Option[String]](s"$businessRegUrl/business-registration/business-tax-registration")(retrieveCurrentProfileHttpReads, hc, ec) recover {
      case ex =>
        logger.error(s"[retrieveCurrentProfile] exception returned '${ex.getMessage}' user directed to OTRS")
        None
    }
  }
}
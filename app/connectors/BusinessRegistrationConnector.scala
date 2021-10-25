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

import config.FrontendAppConfig
import play.api.Logging
import play.api.libs.json.JsObject
import uk.gov.hmrc.http.{HttpClient, _}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class BusinessRegistrationConnector @Inject()(val appConfig: FrontendAppConfig,
                                              val wSHttp: HttpClient) extends Logging {
  lazy val businessRegUrl: String = appConfig.config.baseUrl("business-registration")

  def retrieveCurrentProfile(implicit hc: HeaderCarrier, rds: HttpReads[HttpResponse]): Future[Option[String]] = {

    wSHttp.GET[HttpResponse](s"$businessRegUrl/business-registration/business-tax-registration") map { profile =>
      if (profile.status == 200) {
        (profile.json.validate[JsObject].get \ "registrationID").validate[String].fold({ invalid =>
          logger.error(s"[BusinessRegistrationConnector] [retrieveCurrentProfile] json returned from BR does not contain registrationID, user will redirect to OTRS")
          None
        }, s => Some(s))
      } else {
        logger.info(s"[BusinessRegistrationConnector] [retrieveCurrentProfile] status not 200, actually ${profile.status} user directed to OTRS")
        Option.empty[String]
      }
    } recover {
      case ex: NotFoundException =>
        Option.empty[String]
      case ex =>
        logger.error(s"[BusinessRegistrationConnector] [retrieveCurrentProfile] exception returned ${ex.getMessage} user directed to OTRS")
        Option.empty[String]
    }
  }
}
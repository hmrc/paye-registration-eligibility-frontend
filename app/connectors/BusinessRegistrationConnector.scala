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

import config.FrontendAppConfig
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.JsObject
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BusinessRegistrationConnector @Inject()(val appConfig: FrontendAppConfig,
                                              val wSHttp: HttpClient) {
  lazy val businessRegUrl = appConfig.config.baseUrl("business-registration")

  def retrieveCurrentProfile(implicit hc: HeaderCarrier, rds: HttpReads[HttpResponse]): Future[Option[String]] = {

    wSHttp.GET[HttpResponse](s"$businessRegUrl/business-registration/business-tax-registration") map { profile =>
      if(profile.status == 200) {
        (profile.json.validate[JsObject].get \ "registrationID").validate[String].fold({invalid =>
          Logger.error(s"[BusinessRegistrationConnector] [retrieveCurrentProfile] json returned from BR does not contain registrationID, user will redirect to OTRS")
          None
        }, s => Some(s))
      } else {
        Logger.info(s"[BusinessRegistrationConnector] [retrieveCurrentProfile] status not 200, actually ${profile.status} user directed to OTRS")
        Option.empty[String]
      }
    } recover {
      case ex: NotFoundException =>
        Option.empty[String]
      case ex =>
        Logger.error(s"[BusinessRegistrationConnector] [retrieveCurrentProfile] exception returned ${ex.getMessage} user directed to OTRS")
        Option.empty[String]
    }
  }
}
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

import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.json.{Reads, __}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.Logging

trait BusinessRegistrationHttpParsers extends Logging {

  val retrieveCurrentProfileHttpReads: HttpReads[Option[String]] = (_: String, _: String, response: HttpResponse) =>
    response.status match {
      case OK =>
        val reads: Reads[String] = (__ \ "registrationID").read[String]
        response.json.validate[String](reads).fold({ _ =>
          logger.error(s"[retrieveCurrentProfileHttpReads] json returned from BR does not contain registrationID, user will redirect to OTRS")
          None
        }, s => Some(s))
      case NOT_FOUND =>
        None
      case status =>
        logger.error(s"[retrieveCurrentProfileHttpReads] status not 200, actually $status user directed to OTRS")
        None
    }
}

object BusinessRegistrationHttpParsers extends BusinessRegistrationHttpParsers

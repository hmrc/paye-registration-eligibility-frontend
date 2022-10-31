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
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.Logging

trait CompanyRegistrationHttpParsers extends Logging {

  val companyRegistrationStatusAndPaymentRefHttpParser: HttpReads[(Option[String], Option[String])] = (_: String, _: String, response: HttpResponse) => {
    response.status match {
      case OK =>
        val statusAndPaymentRef = for {
          status <- (response.json \ "status").validate[String]
          paymentRef <- (response.json \ "confirmationReferences" \ "payment-reference").validateOpt[String]
        } yield (status, paymentRef)

        statusAndPaymentRef.fold({ _ =>
          logger.error(s"[companyRegistrationStatusAndPaymentRefHttpParser] json returned from CR does not contain status, user will redirect to OTRS")
          None -> None
        }, s => Some(s._1) -> s._2)
      case NOT_FOUND =>
        None -> None
      case status =>
        logger.error(s"[companyRegistrationStatusAndPaymentRefHttpParser] status not 200, actually $status user directed to OTRS")
        None -> None
    }
  }
}

object CompanyRegistrationHttpParsers extends CompanyRegistrationHttpParsers

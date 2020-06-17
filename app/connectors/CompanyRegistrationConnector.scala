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
import play.api.libs.json._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import utils.PREFEFeatureSwitches

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CompanyRegistrationConnector @Inject()(val featureSwitch: PREFEFeatureSwitches,
                                             val http: HttpClient,
                                             val appConfig: FrontendAppConfig) {
  lazy val companyRegistrationUrl: String = appConfig.config.baseUrl("company-registration")
  lazy val companyRegistrationUri: String = appConfig.config.getConfString("company-registration.uri",throw new Exception("company-registration.uri doesn't exist"))
  lazy val stubUrl: String = appConfig.config.baseUrl("incorporation-frontend-stubs")
  lazy val stubUri: String = appConfig.config.getConfString("incorporation-frontend-stubs.uri",throw new Exception("incorporation-frontend-stubs.uri doesn't exist"))

  def getCompanyRegistrationStatusAndPaymentRef(regId: String)(implicit hc: HeaderCarrier): Future[(Option[String],Option[String])] = {

    val url = if (useCompanyRegistration) s"$companyRegistrationUrl$companyRegistrationUri/corporation-tax-registration" else s"$stubUrl$stubUri"

    http.GET[JsObject](s"$url/$regId/corporation-tax-registration") map { response =>
      val statusAndPaymentRef = for {
        status     <- (response \ "status").validate[String]
        paymentRef <- (response \ "confirmationReferences" \ "payment-reference").validateOpt[String]
      } yield (status, paymentRef)

      statusAndPaymentRef.fold({invalid =>
        Logger.error(s"[CompanyRegConnector] [getCompanyRegistrationDetails] json returned from CR does not contain status, user will redirect to OTRS")
        (None,None)
      }, s => (Some(s._1), s._2))
    } recover {
      case ex =>
        Logger.error(s"[CompanyRegConnector] [getCompanyRegistrationDetails] ${ex.getMessage}")
        (None, None)
    }
  }

  private[connectors] def useCompanyRegistration: Boolean = featureSwitch.companyReg.value.toBoolean
}
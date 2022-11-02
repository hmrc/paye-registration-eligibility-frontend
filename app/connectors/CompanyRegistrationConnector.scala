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

package connectors


import config.AppConfig
import connectors.httpParsers.CompanyRegistrationHttpParsers
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.PREFEFeatureSwitches

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CompanyRegistrationConnector @Inject()(val featureSwitch: PREFEFeatureSwitches,
                                             val http: HttpClient,
                                             val appConfig: AppConfig) extends CompanyRegistrationHttpParsers {

  lazy val companyRegistrationUrl: String = appConfig.config.baseUrl("company-registration")
  lazy val companyRegistrationUri: String = appConfig.config.getConfString("company-registration.uri", throw new Exception("company-registration.uri doesn't exist"))
  lazy val stubUrl: String = appConfig.config.baseUrl("incorporation-frontend-stubs")
  lazy val stubUri: String = appConfig.config.getConfString("incorporation-frontend-stubs.uri", throw new Exception("incorporation-frontend-stubs.uri doesn't exist"))

  def getCompanyRegistrationStatusAndPaymentRef(regId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[(Option[String], Option[String])] = {

    val url = if (useCompanyRegistration) s"$companyRegistrationUrl$companyRegistrationUri/corporation-tax-registration" else s"$stubUrl$stubUri"

    http.GET[(Option[String], Option[String])](s"$url/$regId/corporation-tax-registration")(companyRegistrationStatusAndPaymentRefHttpParser, hc, ec) recover {
      case ex =>
        logger.error(s"[getCompanyRegistrationStatusAndPaymentRef] ${ex.getMessage}")
        None -> None
    }
  }

  private[connectors] def useCompanyRegistration: Boolean = featureSwitch.companyReg.value.toBoolean
}
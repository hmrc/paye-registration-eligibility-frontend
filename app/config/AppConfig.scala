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

package config

import controllers.routes
import play.api.i18n.Lang
import play.api.mvc.Call
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject()(val config: ServicesConfig) {

  private def loadConfig(key: String) = config.getString(key)

  lazy val reportAProblemUrl = "https://www.tax.service.gov.uk/contact/report-technical-problem?service=SCRS"

  lazy val authUrl: String = config.baseUrl("auth")
  lazy val loginUrl: String = loadConfig("urls.login")
  lazy val loginContinueUrl: String = loadConfig("urls.loginContinue")

  lazy val payeRegFEUrl: String = loadConfig("microservice.services.paye-registration-frontend.url")
  lazy val compRegFEUrl: String = loadConfig("microservice.services.company-registration-frontend.url")
  lazy val otrsUrl: String = loadConfig("urls.otrsUrl")
  lazy val payeRegFEUri: String = loadConfig("microservice.services.paye-registration-frontend.uri")
  lazy val compRegFEUri: String = loadConfig("microservice.services.company-registration-frontend.uri")
  lazy val payeRegFEStartLink: String = loadConfig("microservice.services.paye-registration-frontend.start-paye")
  lazy val compRegFEStartLink: String = loadConfig("microservice.services.company-registration-frontend.start-ct")
  lazy val scrsFeedbackLink = "https://www.tax.service.gov.uk/contact/beta-feedback?service=SCRS"

  def languageTranslationEnabled: Boolean = sys.props.get("microservice.services.features.welsh-translation").fold(false)(_.toBoolean)

  def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy"))

  def accessibilityStatementRoute(pageUri: String) = s"$payeRegFEUrl$payeRegFEUri/accessibility-statement?pageUri=$pageUri"

  lazy val taxYearStartDate: String = config.getString("tax-year-start-date")
  lazy val currentPayeWeeklyThreshold: Int = config.getInt("paye.weekly-threshold")
  lazy val oldPayeWeeklyThreshold: Int = config.getInt("paye.old-weekly-threshold")

}
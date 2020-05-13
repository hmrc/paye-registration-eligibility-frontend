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

package config

import com.google.inject.Inject
import controllers.routes
import play.api.i18n.Lang
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.config.ServicesConfig

class FrontendAppConfig @Inject()(override val runModeConfiguration: Configuration, environment: Environment) extends ServicesConfig {

  override protected def mode = environment.mode

  private def loadConfig(key: String) = runModeConfiguration.getString(key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  private lazy val contactHost = runModeConfiguration.getString("contact-frontend.host").getOrElse("")
  private val contactFormServiceIdentifier = "scrs"

  lazy val analyticsToken = loadConfig(s"google-analytics.token")
  lazy val analyticsHost = loadConfig(s"google-analytics.host")
  lazy val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"

  lazy val authUrl = baseUrl("auth")
  lazy val loginUrl = loadConfig("urls.login")
  lazy val loginContinueUrl = loadConfig("urls.loginContinue")

  lazy val payeRegFEUrl       = loadConfig("microservice.services.paye-registration-frontend.url")
  lazy val compRegFEUrl       = loadConfig("microservice.services.company-registration-frontend.url")
  lazy val otrsUrl            = loadConfig("urls.otrsUrl")
  lazy val payeRegFEUri       = loadConfig("microservice.services.paye-registration-frontend.uri")
  lazy val compRegFEUri       = loadConfig("microservice.services.company-registration-frontend.uri")
  lazy val payeRegFEStartLink = loadConfig("microservice.services.paye-registration-frontend.start-paye")
  lazy val compRegFEStartLink = loadConfig("microservice.services.company-registration-frontend.start-ct")
  lazy val feedbackLink = loadConfig("microservice.services.paye-registration-frontend.feedback")

  def languageTranslationEnabled: Boolean = sys.props.get("microservice.services.features.welsh-translation").fold(false)(_.toBoolean)
  def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy"))
  def routeToSwitchLanguage = (lang: String) => routes.LanguageSwitchController.switchToLanguage(lang)

  def accessibilityStatementRoute(pageUri: String) = s"$payeRegFEUrl$payeRegFEUri/accessibility-statement?pageUri=$pageUri"

}

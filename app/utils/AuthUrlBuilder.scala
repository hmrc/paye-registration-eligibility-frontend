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

package utils

import config.FrontendAppConfig
import controllers.routes
import javax.inject.Inject
import play.api.mvc.Result
import play.api.mvc.Results.Redirect

class AuthUrlBuilderImpl @Inject()(val appConfig: FrontendAppConfig) extends AuthUrlBuilder

trait AuthUrlBuilder {
  val appConfig: FrontendAppConfig
  private lazy val loginCallback = appConfig.getConfString("auth.login-callback.url",throw new Exception(s"cant find config value - $configRoot.auth.login-callback.url"))
  private lazy val appName        = appConfig.getString("appName")
  private val configRoot          = "microservice.services"

  private lazy val buildCompanyAuthUrl = {
    val companyAuthHost = appConfig.getConfString("company-auth.url",throw new Exception(s"cant find config value - $configRoot.company-auth.url"))
    val loginPath = appConfig.getConfString("auth.login_path",throw new Exception(s"cant find config value - $configRoot.auth.login_path"))

    s"$companyAuthHost$loginPath"
  }

  private lazy val continueUrl = s"$loginCallback${routes.RegisterForPayeController.continueToPayeOrOTRS()}"
  lazy val redirectToLogin: Result = Redirect(buildCompanyAuthUrl, Map(
    "continue" -> Seq(continueUrl),
    "origin"   -> Seq(appName)
  ))
}
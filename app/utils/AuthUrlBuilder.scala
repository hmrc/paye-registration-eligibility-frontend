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

package utils

import config.FrontendAppConfig
import controllers.routes
import javax.inject.Inject
import play.api.mvc.Result
import play.api.mvc.Results.Redirect

class AuthUrlBuilderImpl @Inject()(val appConfig: FrontendAppConfig) extends AuthUrlBuilder

trait AuthUrlBuilder {
  val appConfig: FrontendAppConfig
  private lazy val loginCallback = appConfig.config.getConfString("auth.login-callback.url", throw new Exception(s"cant find config value - $configRoot.auth.login-callback.url"))
  private lazy val appName = appConfig.config.getString("appName")
  private val configRoot = "microservice.services"

  private lazy val buildCompanyAuthUrl = {
    val basGatewayHost = appConfig.config.getConfString("bas-gateway.url", throw new Exception(s"cant find config value - $configRoot.bas-gateway.url"))
    val loginPath = appConfig.config.getConfString("auth.login_path", throw new Exception(s"cant find config value - $configRoot.auth.login_path"))

    s"$basGatewayHost$loginPath"
  }

  private lazy val continueUrl = s"$loginCallback${routes.RegisterForPayeController.continueToPayeOrOTRS()}"
  lazy val redirectToLogin: Result = Redirect(buildCompanyAuthUrl, Map(
    "continue_url" -> Seq(continueUrl),
    "origin" -> Seq(appName)
  ))
}
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

package controllers

import config.AppConfig
import play.api.i18n.{I18nSupport, Lang}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.language.LanguageUtils

import javax.inject.{Inject, Singleton}

@Singleton
class IndexController @Inject()(
                                 val appConfig: AppConfig,
                                 controllerComponents: MessagesControllerComponents,
                                 languageUtils: LanguageUtils
                               ) extends FrontendController(controllerComponents) with I18nSupport {

  private val welsh = Lang("cy")
  private val english = "english"

  def onPageLoad: Action[AnyContent] = Action { implicit request =>
    //TODO Remove when Welsh FS is removed
    if ((languageUtils.getCurrentLang == welsh) && !appConfig.languageTranslationEnabled) {
      Redirect(controllers.routes.LanguageSwitchController.setLanguage(english))
    } else {
      Redirect(controllers.routes.AtLeastOneDirectorHasNinoController.onPageLoad)
    }
  }
}

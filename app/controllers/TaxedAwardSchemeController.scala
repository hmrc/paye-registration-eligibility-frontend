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

package controllers

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.actions._
import forms.TaxedAwardSchemeFormProvider
import identifiers.TaxedAwardSchemeId
import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Navigator, UserAnswers}
import views.html.taxedAwardScheme

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class TaxedAwardSchemeController @Inject()(appConfig: FrontendAppConfig,
                                           dataCacheConnector: DataCacheConnector,
                                           identify: SessionAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           formProvider: TaxedAwardSchemeFormProvider,
                                           controllerComponents: MessagesControllerComponents
                                          ) extends FrontendController(controllerComponents) with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad() = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.taxedAwardScheme match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Ok(taxedAwardScheme(appConfig, preparedForm))
  }

  def onSubmit() = (identify andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(taxedAwardScheme(appConfig, formWithErrors))),
        (value) =>
          dataCacheConnector.save[Boolean](request.internalId, TaxedAwardSchemeId.toString, value).map { cacheMap =>
            Redirect(Navigator.nextPage(TaxedAwardSchemeId)(new UserAnswers(cacheMap)))
          }
      )
  }
}

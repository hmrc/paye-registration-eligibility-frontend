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
import connectors.{BusinessRegistrationConnector, CompanyRegistrationConnector}
import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.mvc.{MessagesControllerComponents, Result}
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{AuthUrlBuilder, DateUtil}
import views.html.registerForPaye

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class RegisterForPayeController @Inject()(val appConfig: FrontendAppConfig,
                                          val authConnector: AuthConnector,
                                          val authUrlBuilder: AuthUrlBuilder,
                                          val businessRegistrationConnector: BusinessRegistrationConnector,
                                          val companyRegistrationConnector: CompanyRegistrationConnector,
                                          controllerComponents: MessagesControllerComponents
<<<<<<< HEAD
                                         ) extends FrontendController(controllerComponents) with I18nSupport with AuthorisedFunctions {
  lazy val payeStartUrl = s"${appConfig.payeRegFEUrl}${appConfig.payeRegFEUri}${appConfig.payeRegFEStartLink}"
  lazy val otrsUrl = appConfig.otrsUrl
=======
                                             ) extends FrontendController(controllerComponents) with I18nSupport with AuthorisedFunctions {
  lazy  val payeStartUrl            = s"${appConfig.payeRegFEUrl}${appConfig.payeRegFEUri}${appConfig.payeRegFEStartLink}"
  lazy val otrsUrl                  = appConfig.otrsUrl
>>>>>>> origin/SAR-5687

  def onPageLoad = Action {
    implicit request =>
      val notLoggedIn = hc.authorization.isEmpty
      Ok(registerForPaye(appConfig, DateUtil.isInTaxYearPeriod, notLoggedIn, DateUtil.getCurrentPayeThreshold))
  }

  def onSubmit = Action.async {
    implicit request =>
      authorised() {
        Future.successful(Redirect(controllers.routes.RegisterForPayeController.continueToPayeOrOTRS))
      } recoverWith {
        case _ =>
          Future.successful(authUrlBuilder.redirectToLogin)
      }
  }

  private def navigateBasedOnStatusAndPaymentRef(statusAndPaymentRef: (Option[String], Option[String])): Result = {
    statusAndPaymentRef match {
      case (Some(_), Some(_)) => Redirect(payeStartUrl)
      case _ => Redirect(otrsUrl)
    }
  }

  def continueToPayeOrOTRS = Action.async {
    implicit request =>
      authorised() {
        businessRegistrationConnector.retrieveCurrentProfile.flatMap { reg =>
          reg.fold(Future.successful(Redirect(otrsUrl)))(
            regId =>
              companyRegistrationConnector.getCompanyRegistrationStatusAndPaymentRef(regId).map {
                status =>
                  navigateBasedOnStatusAndPaymentRef(status)
              }
          )
        }
      } recover {
        case _ => Redirect(routes.IndexController.onPageLoad())
      }
  }
}
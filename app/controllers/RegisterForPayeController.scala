/*
 * Copyright 2023 HM Revenue & Customs
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
import connectors.{BusinessRegistrationConnector, CompanyRegistrationConnector}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.{AuthUrlBuilder, TimeMachine}
import views.html.registerForPaye

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegisterForPayeController @Inject()(val authConnector: AuthConnector,
                                          val authUrlBuilder: AuthUrlBuilder,
                                          val businessRegistrationConnector: BusinessRegistrationConnector,
                                          val companyRegistrationConnector: CompanyRegistrationConnector,
                                          controllerComponents: MessagesControllerComponents,
                                          timeMachine: TimeMachine,
                                          view: registerForPaye
                                         )(implicit appConfig: AppConfig, implicit val ec:ExecutionContext) extends FrontendController(controllerComponents) with I18nSupport with AuthorisedFunctions {

  lazy val payeStartUrl: String = s"${appConfig.payeRegFEUrl}${appConfig.payeRegFEUri}${appConfig.payeRegFEStartLink}"
  lazy val otrsUrl: String = appConfig.otrsUrl

  def onPageLoad: Action[AnyContent] = Action {
    implicit request =>
      val notLoggedIn = hc.authorization.isEmpty
      Ok(view(timeMachine.isInTaxYearPeriod, notLoggedIn, timeMachine.getCurrentPayeThreshold))
  }

  def onSubmit: Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        Future.successful(Redirect(controllers.routes.RegisterForPayeController.continueToPayeOrOTRS()))
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

  def continueToPayeOrOTRS: Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        businessRegistrationConnector.retrieveCurrentProfile.flatMap {
          reg =>
            reg.fold(Future.successful(Redirect(otrsUrl)))(
              regId =>
                companyRegistrationConnector.getCompanyRegistrationStatusAndPaymentRef(regId).map {
                  status =>
                    navigateBasedOnStatusAndPaymentRef(status)
                }
            )
        }
      } recover {
        case _ => Redirect(routes.IndexController.onPageLoad)
      }
  }
}
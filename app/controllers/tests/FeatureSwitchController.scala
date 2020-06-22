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

package controllers.tests

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils._

import scala.concurrent.Future

@Singleton
class FeatureSwitchController @Inject()(val featureManager: FeatureManager,
                                        val prefeFeatureSwitch: PREFEFeatureSwitches,
                                        controllerComponents: MessagesControllerComponents) extends FrontendController(controllerComponents) {

  def switcher(featureName: String, featureState: String): Action[AnyContent] = Action.async {
    implicit request =>
      def feature: FeatureSwitch = featureState match {
        case x if x.matches(DateUtil.datePatternRegex) => featureManager.setSystemDate(ValueSetFeatureSwitch(featureName, featureState))
        case "true" | "false" => featureManager.enableORDisable(BooleanFeatureSwitch(featureName, featureState.toBoolean))
        case _ if featureName == "system-date" => featureManager.clearSystemDate(ValueSetFeatureSwitch(featureName, "time-clear"))
        case _ => featureManager.enableORDisable(BooleanFeatureSwitch(featureName, false))
      }

      prefeFeatureSwitch(featureName) match {
        case Some(_) => Future.successful(Ok(feature.toString))
        case None => Future.successful(BadRequest)
      }
  }
}


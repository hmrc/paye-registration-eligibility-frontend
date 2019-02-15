/*
 * Copyright 2019 HM Revenue & Customs
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



import controllers.routes
import identifiers._
import play.api.mvc.Call

object Navigator {

  private[utils] val pageIdToLoad: (Identifier => Call) = {
    case AtLeastOneDirectorHasNinoId => controllers.routes.AtLeastOneDirectorHasNinoController.onPageLoad()
    case OffshoreEmployerId          => controllers.routes.OffshoreEmployerController.onPageLoad()
    case TaxedAwardSchemeId          => controllers.routes.TaxedAwardSchemeController.onPageLoad()
    case RegisterForPayeId           => controllers.routes.RegisterForPayeController.onPageLoad()
    case IneligibleId                => controllers.routes.IneligibleController.onPageLoad()
  }

  private[utils] def nextOn(condition: Boolean, fromPage: Identifier, toPage: Identifier): (Identifier, UserAnswers => Call) = {
    fromPage -> {
      _.getAnswer(fromPage) match {
        case Some(`condition`) => pageIdToLoad(toPage)
        case _ => pageIdToLoad(IneligibleId)
      }
    }
  }

  private val routeMap: Map[Identifier, UserAnswers => Call] = Map(
    nextOn(true, AtLeastOneDirectorHasNinoId, OffshoreEmployerId),
    nextOn(false, OffshoreEmployerId, TaxedAwardSchemeId),
    nextOn(false, TaxedAwardSchemeId, RegisterForPayeId)
  )

  def nextPage(id: Identifier): UserAnswers => Call =
    routeMap.getOrElse(id, _ => routes.IndexController.onPageLoad())

}

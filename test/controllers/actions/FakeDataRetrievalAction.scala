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

package controllers.actions

import connectors.FakeDataCacheConnector
import models.requests.{CacheIdentifierRequest, OptionalDataRequest}
import play.api.mvc.MessagesControllerComponents
import repositories.SessionRepository
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.{CascadeUpsert, UserAnswers}

import scala.concurrent.{ExecutionContext, Future}

class FakeDataRetrievalAction(cacheMapToReturn: Option[CacheMap],
                              controllerComponents: MessagesControllerComponents,
                              sessionRepository: SessionRepository,
                              cascadeUpsert: CascadeUpsert
                             ) (implicit override val ec: ExecutionContext) extends DataRetrievalAction(new FakeDataCacheConnector(sessionRepository, cascadeUpsert), controllerComponents) {
  override protected def transform[A](request: CacheIdentifierRequest[A]): Future[OptionalDataRequest[A]] = cacheMapToReturn match {
    case None => Future(OptionalDataRequest(request.request, request.cacheId, None))
    case Some(cacheMap) => Future(OptionalDataRequest(request.request, request.cacheId, Some(new UserAnswers(cacheMap))))
  }

  override val executionContext: ExecutionContext = controllerComponents.executionContext
}
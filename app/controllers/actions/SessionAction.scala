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

package controllers.actions

import com.google.inject.Inject
import controllers.routes
import models.requests.CacheIdentifierRequest
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SessionAction @Inject()(controllerComponents: MessagesControllerComponents)
  extends ActionBuilder[CacheIdentifierRequest, AnyContent] with ActionFunction[Request, CacheIdentifierRequest] {

  override def invokeBlock[A](request: Request[A], block: CacheIdentifierRequest[A] => Future[Result]): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    hc.sessionId match {
      case Some(session) => block(CacheIdentifierRequest(request, session.value))
      case None => Future.successful(Redirect(routes.SessionExpiredController.onPageLoad))
    }
  }

  override protected val executionContext: ExecutionContext = controllerComponents.executionContext

  override val parser: BodyParser[AnyContent] = controllerComponents.parsers.defaultBodyParser
}

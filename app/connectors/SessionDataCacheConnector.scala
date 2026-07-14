/*
 * Copyright 2026 HM Revenue & Customs
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

package connectors

import identifiers.CacheKeys._
import repositories.SessionCacheRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.cache.DataKey
import utils.UserAnswers

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SessionDataCacheConnector @Inject()(cacheRepository: SessionCacheRepository)(implicit ec: ExecutionContext) {

  import cacheRepository._

  def saveValueToSession(value: Boolean, key: DataKey[Boolean])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[UserAnswers]] =
    putSession[Boolean](key, value).flatMap(_ => fetchUserAnswersFromSession)

  def fetchUserAnswersFromSession(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[UserAnswers]] =
    for {
      taxedAwardSchemeOpt <- getFromSession[Boolean](taxedAwardScheme)
      offshoreEmployerOpt <- getFromSession[Boolean](offshoreEmployer)
      atLeastOneDirectorHasNinoOpt <- getFromSession[Boolean](atLeastOneDirectorHasNino)
    } yield {
      if (taxedAwardSchemeOpt.isEmpty && offshoreEmployerOpt.isEmpty && atLeastOneDirectorHasNinoOpt.isEmpty) {
        None
      } else {
        Some(UserAnswers(taxedAwardSchemeOpt,  offshoreEmployerOpt, atLeastOneDirectorHasNinoOpt))
      }
    }

}

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

package utils

import config.AppConfig

import java.time.LocalDate
import javax.inject.{Inject, Singleton}

@Singleton
class TimeMachine @Inject()(appConfig: AppConfig) {

  val taxYearStart: LocalDate = LocalDate.parse(appConfig.taxYearStartDate)

  def startPeriod: LocalDate = LocalDate.of(now.getYear, 2, 6)

  def now: LocalDate = LocalDate.now()

  def isInTaxYearPeriod: Boolean = ((now isEqual startPeriod) | (now isAfter startPeriod)) & (now isBefore taxYearStart)

  def getCurrentPayeThreshold: String =
    if (now.isEqual(taxYearStart) || now.isAfter(taxYearStart)) {
      appConfig.currentPayeWeeklyThreshold.toString
    } else {
      appConfig.oldPayeWeeklyThreshold.toString
    }

}
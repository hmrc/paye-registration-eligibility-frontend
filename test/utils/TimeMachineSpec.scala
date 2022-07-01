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

package utils

import config.AppConfig
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.LocalDate

class TimeMachineSpec extends PlaySpec with BeforeAndAfterEach with MockitoSugar {

  object TestAppConfig extends AppConfig(mock[ServicesConfig], mock[PREFEFeatureSwitches]) {
    override lazy val taxYearStartDate: String = LocalDate.now().toString
    override lazy val currentPayeWeeklyThreshold: Int = 10
    override lazy val oldPayeWeeklyThreshold: Int = 5
  }

  "getCurrentThresholds" should {
    "return the old tax years thresholds if the date is before the tax year start date" in {
      object TestTimeMachine extends TimeMachine(TestAppConfig) {
        override def now: LocalDate = LocalDate.now().minusDays(1)
      }

      val result = TestTimeMachine.getCurrentPayeThreshold
      result mustBe "5"
    }

    "return the new tax years thresholds if the date is on the tax year start date" in {
      object TestTimeMachine extends TimeMachine(TestAppConfig)

      val result = TestTimeMachine.getCurrentPayeThreshold
      result mustBe "10"
    }

    "return the new tax years thresholds if the date is after the tax year start date" in {
      object TestTimeMachine extends TimeMachine(TestAppConfig) {
        override def now: LocalDate = LocalDate.now().plusDays(1)
      }

      val result = TestTimeMachine.getCurrentPayeThreshold
      result mustBe "10"
    }
  }
}

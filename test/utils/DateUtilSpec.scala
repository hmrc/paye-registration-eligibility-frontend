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

package utils

import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec

class DateUtilSpec extends PlaySpec with BeforeAndAfterEach {

  "getPayeThresholds" should {

    "return the 2017-2018 tax year thresholds if the system date is 2019-03-02" in {
      System.setProperty("feature.system-date", "2019-03-02")

      val result = utils.DateUtil.getCurrentPayeThreshold
      result mustBe "116"
    }

    "return the 2017-2018 year thresholds if the system date is 2019-04-05" in {
      System.setProperty("feature.system-date", "2019-04-05")

      val result = utils.DateUtil.getCurrentPayeThreshold
      result mustBe "116"
    }

    "return the 2018-2019 tax year thresholds if the system date is 2019-04-06" in  {
      System.setProperty("feature.system-date", "2019-04-06")

      val result = utils.DateUtil.getCurrentPayeThreshold
      result mustBe "118"
    }

    "return the 2018-2019 tax year thresholds if the system date is 2019-10-26" in  {
      System.setProperty("feature.system-date", "2019-10-26")

      val result = utils.DateUtil.getCurrentPayeThreshold
      result mustBe "118"
    }
  }
}

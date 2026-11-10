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

import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec

class FeatureSwitchSpec extends PlaySpec with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    System.clearProperty("feature.test")
    System.clearProperty("feature.cohoFirstHandOff")
    System.clearProperty("feature.businessActivitiesHandOff")
    System.clearProperty("feature.system-date")
    System.clearProperty("feature.companyRegistration")
    System.clearProperty("feature.isWelsh")
    System.clearProperty("feature.userResearchBanner")
    super.beforeEach()
  }

  override def afterEach(): Unit = {
    System.clearProperty("feature.companyRegistration")
    System.clearProperty("feature.isWelsh")
    System.clearProperty("feature.userResearchBanner")
    System.clearProperty("feature.system-date")
    super.afterEach()
  }

  val featureSwitch = new FeatureSwitchManager
  val prefeFeatureSwitch = new PREFEFeatureSwitch(featureSwitch)
  val valueFeatureSwitch: ValueSetFeatureSwitch = ValueSetFeatureSwitch("test", "2018-10-12")

  "getProperty" should {
    "return a value feature switch with time-clear if the system property is undefined" in {
      featureSwitch.getProperty("test", "") mustBe ValueSetFeatureSwitch("test", "time-clear")
    }

    "return an value feature switch with a date if the system property is defined as '2018-10-12'" in {
      System.setProperty("feature.test", "2018-10-12")

      featureSwitch.getProperty("test", "") mustBe ValueSetFeatureSwitch("test", "2018-10-12")
    }

    "return an value feature switch with time-clear if the system property is defined as 'time-clear'" in {
      System.setProperty("feature.test", "time-clear")

      featureSwitch.getProperty("test", "") mustBe ValueSetFeatureSwitch("test", "time-clear")
    }
  }

  "systemPropertyName" should {
    "append feature. to the supplied string'" in {
      featureSwitch.systemPropertyName("test") mustBe "feature.test"
    }
  }

  "setProperty" should {

    "return a feature switch (testKey, time-clear) when supplied with (testKey, testValue)" in {
      featureSwitch.setProperty("test", "testValue", "") mustBe ValueSetFeatureSwitch("test", "time-clear")
    }

    "return ValueSetFeatureSwitch when supplied system-date and 2018-01-01" in {
      featureSwitch.setProperty("system-date", "2018-01-01", "") mustBe ValueSetFeatureSwitch("system-date", "2018-01-01")
    }
  }

  "setSystemDate" should {
    "set the value for the supplied key to the date provided" in {
      System.setProperty("feature.test", "time-clear")

      featureSwitch.setSystemDate(valueFeatureSwitch) mustBe ValueSetFeatureSwitch("test", "2018-10-12")
    }
  }

  "clearSystemDate" should {
    "set the value for the supplied key to 'false'" in {
      System.setProperty("feature.test", "2018-10-12")

      featureSwitch.clearSystemDate(valueFeatureSwitch) mustBe ValueSetFeatureSwitch("test", "time-clear")
    }
  }

  "ValueSetFeatureSwitch" should {
    "return the set value as its value" in {
      ValueSetFeatureSwitch("test", "2018-10-12").value mustBe "2018-10-12"
    }

    "be enabled when the set value is a valid date" in {
      ValueSetFeatureSwitch("test", "2018-10-12").enabled mustBe true
    }

    "be disabled when the set value is 'time-clear'" in {
      ValueSetFeatureSwitch("test", "time-clear").enabled mustBe false
    }

    "be disabled when the set value is not a valid date" in {
      ValueSetFeatureSwitch("test", "2018-13-45").enabled mustBe false
    }
  }

  "PREFEFeatureSwitch" when {

    Seq(
      ("companyRegistration", () => prefeFeatureSwitch.companyReg),
      ("isWelsh", () => prefeFeatureSwitch.isWelshEnabled),
      ("userResearchBanner", () => prefeFeatureSwitch.isUserResearchBannerEnabled)
    ).foreach { case (name, switch) =>

      s"reading the $name switch" should {
        "be disabled by default" in {
          switch() mustBe BooleanFeatureSwitch(name, setValue = false)
        }

        "be enabled when the system property is 'true'" in {
          System.setProperty(s"feature.$name", "true")

          switch() mustBe BooleanFeatureSwitch(name, setValue = true)
        }

        "be returned by apply" in {
          System.setProperty(s"feature.$name", "true")

          prefeFeatureSwitch(name) mustBe Some(BooleanFeatureSwitch(name, setValue = true))
        }
      }
    }

    "apply is given system-date" should {
      "return time-clear when no system date is set" in {
        prefeFeatureSwitch("system-date") mustBe Some(ValueSetFeatureSwitch("system-date", "time-clear"))
      }

      "return the date when a system date is set" in {
        System.setProperty("feature.system-date", "2018-10-12")

        prefeFeatureSwitch("system-date") mustBe Some(ValueSetFeatureSwitch("system-date", "2018-10-12"))
      }
    }

    "apply is given an unknown switch name" should {
      "return None" in {
        prefeFeatureSwitch("unknownSwitch") mustBe None
      }
    }
  }

  "BooleanFeatureSwitch" should {
    "return 'true' as its value and be enabled when set to true" in {
      val switch = BooleanFeatureSwitch("test", setValue = true)

      switch.value mustBe "true"
      switch.enabled mustBe true
    }

    "return 'false' as its value and be disabled when set to false" in {
      val switch = BooleanFeatureSwitch("test", setValue = false)

      switch.value mustBe "false"
      switch.enabled mustBe false
    }
  }
}

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

import controllers.ControllerSpecBase
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils._

class FeatureSwitchControllerSpec extends ControllerSpecBase with BeforeAndAfterEach with MockitoSugar {

  val testFeatureSwitch = ValueSetFeatureSwitch("system-date", "2018-10-12")
  val testDisabledSwitch = ValueSetFeatureSwitch("system-date", "time-clear")

  val mockFeatureSwitches = mock[PREFEFeatureSwitches]
  val mockFeatureManager = mock[FeatureManager]

  override def beforeEach() = {
    reset(
      mockFeatureManager, mockFeatureSwitches
    )
  }

  class Setup {
    val controller = new FeatureSwitchControllerImpl(mockFeatureManager, mockFeatureSwitches, messagesControllerComponents)
  }

  "switcher" should {
    "change the system date" when {
      "system-date and a date are passed in the url" in new Setup {
        when(mockFeatureSwitches(any()))
          .thenReturn(Some(testFeatureSwitch))

        when(mockFeatureManager.setSystemDate(any()))
          .thenReturn(testFeatureSwitch)


        val result = controller.switcher("system-date","2018-10-12")(fakeRequest)
        status(result) mustBe OK
      }
    }

    "clear the system date" when {
      "system-date and time-clear are passed in the url" in new Setup {
        when(mockFeatureSwitches(any()))
          .thenReturn(Some(testFeatureSwitch))

        when(mockFeatureManager.clearSystemDate(any()))
          .thenReturn(testDisabledSwitch)

        val result = controller.switcher("system-date","time-clear")(FakeRequest())
        status(result) mustBe OK
      }
    }

    "return a bad request" when {
      "an unknown feature is trying to be enabled" in new Setup {
        when(mockFeatureSwitches(any()))
          .thenReturn(None)

        val result = controller.switcher("invalidName","invalidState")(FakeRequest())
        status(result) mustBe BAD_REQUEST
      }
    }
  }
}

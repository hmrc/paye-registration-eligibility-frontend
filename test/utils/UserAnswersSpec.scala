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

package utils

import base.SpecBase
import identifiers.{AtLeastOneDirectorHasNinoId, OffshoreEmployerId, RegisterForPayeId, TaxedAwardSchemeId}

class UserAnswersSpec extends SpecBase {

  "UserAnswers getAnswer" should {

    "return the taxedAwardScheme answer" in {

      val answers = UserAnswers(
        taxedAwardScheme = Some(true)
      )

      answers.getAnswer(TaxedAwardSchemeId) mustBe Some(true)
    }

    "return the offshoreEmployer answer" in {

      val answers = UserAnswers(
        offshoreEmployer = Some(false)
      )

      answers.getAnswer(OffshoreEmployerId) mustBe Some(false)
    }

    "return the atLeastOneDirectorHasNino answer" in {

      val answers = UserAnswers(
        atLeastOneDirectorHasNino = Some(true)
      )

      answers.getAnswer(AtLeastOneDirectorHasNinoId) mustBe Some(true)
    }

    "return None when the requested answer has not been provided" in {

      val answers = UserAnswers()

      answers.getAnswer(TaxedAwardSchemeId) mustBe None
      answers.getAnswer(OffshoreEmployerId) mustBe None
      answers.getAnswer(AtLeastOneDirectorHasNinoId) mustBe None
    }

    "return None for an unsupported identifier" in {

      val answers = UserAnswers(
        taxedAwardScheme = Some(true),
        offshoreEmployer = Some(false),
        atLeastOneDirectorHasNino = Some(true)
      )

      answers.getAnswer(RegisterForPayeId) mustBe None
    }
  }
}
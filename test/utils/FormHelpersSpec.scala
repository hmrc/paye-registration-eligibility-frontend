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

import org.scalatestplus.play.PlaySpec
import play.api.data.Form
import play.api.data.Forms.{single, text}

class FormHelpersSpec extends PlaySpec {

  val form: Form[String] = Form(single("value" -> text))

  "getErrorByKey" should {
    "return the error message when the form has an error for the key" in {
      val formWithError = form.withError("value", "error.required")

      FormHelpers.getErrorByKey(formWithError, "value") mustBe "error.required"
    }

    "return an empty string when the form has no error for the key" in {
      FormHelpers.getErrorByKey(form, "value") mustBe ""
    }

    "return an empty string when the error is for a different key" in {
      val formWithError = form.withError("other", "error.required")

      FormHelpers.getErrorByKey(formWithError, "value") mustBe ""
    }
  }
}

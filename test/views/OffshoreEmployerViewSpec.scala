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

package views

import forms.OffshoreEmployerFormProvider
import play.api.data.Form
import views.behaviours.YesNoViewBehaviours
import views.html.offshoreEmployer

class OffshoreEmployerViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "offshoreEmployer"

  val form = new OffshoreEmployerFormProvider()()

  val view: offshoreEmployer = app.injector.instanceOf[offshoreEmployer]

  def createView = () => view(form)(fakeRequest(), messages, injectedAppConfig)

  def createViewUsingForm = (form: Form[_]) => view(form)(fakeRequest(), messages, injectedAppConfig)

  "OffshoreEmployer view" must {

    behave like normalPage(createView, messageKeyPrefix)

    behave like yesNoPage(createViewUsingForm, messageKeyPrefix)
  }
}

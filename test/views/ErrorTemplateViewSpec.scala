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

package views

import org.jsoup.nodes.Document
import org.scalatest.BeforeAndAfterEach
import views.html.templates.error_template

class ErrorTemplateViewSpec extends ViewSpecBase with BeforeAndAfterEach {

  val view: error_template = app.injector.instanceOf[error_template]

  val testTitle = "Test title"
  val testHeading = "Test heading"
  val testMessage = "Test message"

  def doc: Document = asDocument(view(testTitle, testHeading, testMessage)(fakeRequest(), messages, injectedAppConfig))

  override def afterEach(): Unit = {
    sys.props -= "feature.userResearchBanner"
    super.afterEach()
  }

  "Error template view" must {

    "display the page title in the browser title" in {
      doc.title must include(testTitle)
    }

    "display the heading" in {
      doc.select("h1").text mustBe testHeading
    }

    "display the message" in {
      assertContainsText(doc, testMessage)
    }

    "display the UR banner with a close button when the userResearchBanner switch is enabled" in {
      sys.props += ("feature.userResearchBanner" -> "true")
      val document = doc

      assertRenderedByCssSelector(document, ".hmrc-user-research-banner")
      assertRenderedByCssSelector(document, ".hmrc-user-research-banner__close")
    }

    "not display the UR banner when the userResearchBanner switch is disabled" in {
      sys.props += ("feature.userResearchBanner" -> "false")

      assertNotRenderedByCssSelector(doc, ".hmrc-user-research-banner")
    }
  }
}

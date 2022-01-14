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

package www

import helpers._
import play.api.http.{HeaderNames, Status}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.crypto.DefaultCookieSigner
import utils.{BooleanFeatureSwitch, PREFEFeatureSwitches}

class RegisterForPayeControllerISpec extends IntegrationSpecBase with SessionHelper with WiremockHelper {

  override val mockHost = WiremockHelper.wiremockHost
  override val mockPort = WiremockHelper.wiremockPort

  def additionalConfiguration: Map[String, String] = Map(
    "play.filters.csrf.header.bypassHeaders.X-Requested-With" -> "*",
    "play.filters.csrf.header.bypassHeaders.Csrf-Token" -> "nocheck",
    "microservice.services.auth.host" -> s"$mockHost",
    "microservice.services.auth.port" -> s"$mockPort",
    "auditing.consumer.baseUri.host" -> s"$mockHost",
    "auditing.consumer.baseUri.port" -> s"$mockPort",
    "microservice.services.paye-registration.host" -> s"$mockHost",
    "microservice.services.paye-registration.port" -> s"$mockPort",
    "microservice.services.company-registration.host" -> s"$mockHost",
    "microservice.services.company-registration.port" -> s"$mockPort",
    "microservice.services.business-registration.host" -> s"$mockHost",
    "microservice.services.business-registration.port" -> s"$mockPort",
    "microservice.services.incorporation-frontend-stubs.port" -> s"$mockPort",
    "microservice.services.incorporation-frontend-stubs.port" -> s"$mockPort",
    "microservice.services.cachable.session-cache.host" -> s"$mockHost",
    "microservice.services.cachable.session-cache.port" -> s"$mockPort",
    "microservice.services.cachable.session-cache.domain" -> "keystore",
    "microservice.services.cachable.short-lived-cache.host" -> s"$mockHost",
    "microservice.services.cachable.short-lived-cache.port" -> s"$mockPort",
    "microservice.services.cachable.short-lived-cache.domain" -> "save4later",
    "mongodb.uri" -> s"$mongoUri",
    "feature.companyRegistration" -> "true"
  )

  override lazy val mockUrl = s"http://$mockHost:$mockPort"
  override val cookieSigner: DefaultCookieSigner = app.injector.instanceOf[DefaultCookieSigner]
  override implicit lazy val app = new GuiceApplicationBuilder().configure(additionalConfiguration).build()

  val sessionCookie = () => getSessionCookie()
  val featureSwitches = app.injector.instanceOf[PREFEFeatureSwitches]
  val regId = "6"
  val txID = "tx1234567"
  val companyName = "Test Company"

  def enableCompanyRegistrationFeature() = buildClient("/test-only/feature-flag/companyRegistration/true").get()

  "submit" should {
    s"redirect to log in page with continue url of ${controllers.routes.RegisterForPayeController.onSubmit.url}" in {

      stubAuthorisation(404)
      stubAudits()
      val fResponse = buildClient("/register-online").
        withHeaders(HeaderNames.COOKIE -> sessionCookie(), "Csrf-Token" -> "nocheck").
        post(Map(
          "csrfToken" -> Seq("xxx-ignored-xxx")
        ))

      val response = await(fResponse)

      response.status mustBe 303
      response.header("Location") mustBe Some("http://localhost:9553/bas-gateway/sign-in?accountType=organisation&continue_url=http%3A%2F%2Flocalhost%3A9877%2Feligibility-for-paye%2Fauthorised-for-paye&origin=paye-registration-eligibility-frontend")

    }
    s"redirect to ${controllers.routes.RegisterForPayeController.continueToPayeOrOTRS.url}" in {

      stubAuthorisation()
      stubAudits()
      val fResponse = buildClient("/register-online").
        withHeaders(HeaderNames.COOKIE -> sessionCookie(), "Csrf-Token" -> "nocheck").
        post(Map(
          "csrfToken" -> Seq("xxx-ignored-xxx")
        ))

      val response = await(fResponse)

      response.status mustBe 303
      response.header("Location") mustBe Some(controllers.routes.RegisterForPayeController.continueToPayeOrOTRS.url)

    }
  }

  s"${controllers.routes.RegisterForPayeController.continueToPayeOrOTRS.url} " should {

    "redirect to otrs when user has logged in but doesn't have a footprint" in {
      stubAuthorisation()
      stubAudits()

      stubGet("/business-registration/business-tax-registration", Status.NOT_FOUND,
        s"""{
           |}
            """.stripMargin
      )

      val fResponse = buildClient("/authorised-for-paye").
        withHeaders(HeaderNames.COOKIE -> sessionCookie(), "Csrf-Token" -> "nocheck").
        get

      val response = await(fResponse)

      response.status mustBe 303
      response.header("Location") mustBe Some("https://www.tax.service.gov.uk/business-registration/select-taxes")

    }

    "redirect to PRFE when user has logged in and has BR record and a CT record with a payment reference" in {
      featureSwitches.manager.enableORDisable(BooleanFeatureSwitch("companyRegistration", true))
      stubAuthorisation()
      stubAudits()

      stubGet("/business-registration/business-tax-registration", Status.OK,
        s"""{
           |  "registrationID": "$regId",
           |  "completionCapacity": "Director",
           |  "language": "EN"
           |}
                  """.stripMargin
      )

      stubGet(s"/company-registration/corporation-tax-registration/$regId/corporation-tax-registration", Status.OK,
        s"""{
           |  "status": "held",
           |  "confirmationReferences": {
           |  "transaction-id": "tx1234567",
           |  "payment-reference": "122334456"
           |  }
           |}
            """.stripMargin

      )

      val fResponse = buildClient("/authorised-for-paye").
        withHeaders(HeaderNames.COOKIE -> sessionCookie(), "Csrf-Token" -> "nocheck").
        get

      val response = await(fResponse)

      response.status mustBe 303
      response.header("Location") mustBe Some("http://localhost:9870/register-for-paye/start-pay-as-you-earn")

    }

    "redirect to OTRS when user has logged in and has BR record and a CT record without a payment reference" in {
      featureSwitches.manager.enableORDisable(BooleanFeatureSwitch("companyRegistration", true))
      stubAuthorisation()
      stubAudits()

      stubGet("/business-registration/business-tax-registration", Status.OK,
        s"""{
           |  "registrationID": "$regId",
           |  "completionCapacity": "Director",
           |  "language": "EN"
           |}
                  """.stripMargin
      )

      stubGet(s"/company-registration/corporation-tax-registration/$regId/corporation-tax-registration", Status.OK,
        s"""{
           |  "status": "held",
           |  "confirmationReferences": {
           |  "transaction-id": "tx1234567"
           |  }
           |}
            """.stripMargin

      )

      val fResponse = buildClient("/authorised-for-paye").
        withHeaders(HeaderNames.COOKIE -> sessionCookie(), "Csrf-Token" -> "nocheck").
        get

      val response = await(fResponse)

      response.status mustBe 303
      response.header("Location") mustBe Some("https://www.tax.service.gov.uk/business-registration/select-taxes")
    }

    "STUB MODE - redirect to OTRS when user has logged in and has BR record and a CT record without a payment reference" in {
      featureSwitches.manager.enableORDisable(BooleanFeatureSwitch("companyRegistration", false))
      stubAuthorisation()
      stubAudits()

      stubGet("/business-registration/business-tax-registration", Status.OK,
        s"""{
           |  "registrationID": "$regId",
           |  "completionCapacity": "Director",
           |  "language": "EN"
           |}
                  """.stripMargin
      )

      stubGet(s"/incorporation-frontend-stubs/$regId/corporation-tax-registration", Status.OK,
        s"""{
           |  "status": "held",
           |  "confirmationReferences": {
           |  "transaction-id": "tx1234567"
           |  }
           |}
            """.stripMargin

      )

      val fResponse = buildClient("/authorised-for-paye").
        withHeaders(HeaderNames.COOKIE -> sessionCookie(), "Csrf-Token" -> "nocheck").
        get

      val response = await(fResponse)

      response.status mustBe 303
      response.header("Location") mustBe Some("https://www.tax.service.gov.uk/business-registration/select-taxes")
    }


    "redirect to OTRS when user has logged in and has BR record but NO CT record" in {
      featureSwitches.manager.enableORDisable(BooleanFeatureSwitch("companyRegistration", true))
      stubAuthorisation()
      stubAudits()

      stubGet("/business-registration/business-tax-registration", status = Status.OK,
        s"""{
           |  "registrationID": "$regId",
           |  "completionCapacity": "Director",
           |  "language": "EN"
           |}
                  """.stripMargin
      )

      stubGet(s"/company-registration/corporation-tax-registration/$regId/corporation-tax-registration", Status.NOT_FOUND,
        s"""{
            }
             """.stripMargin
      )

      val fResponse = buildClient("/authorised-for-paye").
        withHeaders(HeaderNames.COOKIE -> sessionCookie(), "Csrf-Token" -> "nocheck").
        get

      val response = await(fResponse)

      response.status mustBe 303
      response.header("Location") mustBe Some("https://www.tax.service.gov.uk/business-registration/select-taxes")


    }
    "redirect to OTRS when user has logged in but BR returns a 202" in {
      featureSwitches.manager.enableORDisable(BooleanFeatureSwitch("companyRegistration", true))
      stubAuthorisation()
      stubAudits()

      stubGet("/business-registration/business-tax-registration", status = Status.ACCEPTED,
        s"""{
           |  "registrationID": "$regId",
           |  "completionCapacity": "Director",
           |  "language": "EN"
           |}
                  """.stripMargin
      )

      val fResponse = buildClient("/authorised-for-paye").
        withHeaders(HeaderNames.COOKIE -> sessionCookie(), "Csrf-Token" -> "nocheck").
        get

      val response = await(fResponse)

      response.status mustBe 303
      response.header("Location") mustBe Some("https://www.tax.service.gov.uk/business-registration/select-taxes")


    }


  }

}



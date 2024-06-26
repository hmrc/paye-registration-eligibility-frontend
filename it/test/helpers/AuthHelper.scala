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

package test.helpers

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.Application
import play.api.http.HeaderNames
import play.api.libs.crypto.DefaultCookieSigner
import play.api.libs.json.Json
import play.api.libs.ws.WSCookie
import uk.gov.hmrc.crypto.{Crypted, PlainText, SymmetricCryptoFactory}
import uk.gov.hmrc.http.SessionKeys

import java.net.{URLDecoder, URLEncoder}
import java.nio.charset.StandardCharsets

trait AuthHelper extends SessionCookieBaker {

  private[helpers] val defaultUser = "/foo/bar"

  val sessionId = "session-ac4ed3e7-dbc3-4150-9574-40771c4285c1"

  private def cookieData(additionalData: Map[String, String], userId: String = defaultUser): Map[String, String] = {
    Map(
      SessionKeys.sessionId -> sessionId,
      "token" -> "token",
      "ap" -> "GGW",
      SessionKeys.lastRequestTimestamp -> new java.util.Date().getTime.toString,
      SessionKeys.authToken -> "FooBarToken"
    ) ++ additionalData
  }

  def getSessionCookie(additionalData: Map[String, String] = Map(), userId: String = defaultUser): String = {
    cookieValue(cookieData(additionalData, userId))
  }

  def stubPostAuth(url: String, status: Integer, body: Option[String]): StubMapping = {
    stubFor(post(urlMatching(url)).willReturn {
      val resp = aResponse().withStatus(status)
      val respHeaders = if (status == 401) resp.withHeader(HeaderNames.WWW_AUTHENTICATE, """MDTP detail="MissingBearerToken"""") else resp

      body.fold(respHeaders)(b => respHeaders.withBody(b))
    })
  }

  def stubAuthorisation(status: Int = 200, resp: Option[String] = None): StubMapping = {
    stubPostAuth("/write/audit", 200, Some("""{"x":2}"""))
    stubPostAuth(
      url = "/auth/authorise",
      status = status,
      body = resp match {
        case Some(_) => resp
        case None => Some(Json.obj("authorise" -> Json.arr(), "retrieve" -> Json.arr()).toString())
      }
    )
  }


  def stubAudits(): StubMapping = {
    stubFor(post(urlMatching("/write/audit"))
      .willReturn(
        aResponse().
          withStatus(204)
      )
    )

    stubFor(post(urlMatching("/write/audit/merged"))
      .willReturn(
        aResponse().
          withStatus(204)
      )
    )
  }
}

trait SessionCookieBaker {
  val app: Application
  val cookieSigner: DefaultCookieSigner

  val cookieKey = "gvBoGdgzqG1AarzF1LY0zQ=="

  def cookieValue(sessionData: Map[String, String]): String = {
    def encode(data: Map[String, String]): PlainText = {
      val encoded = data.map {
        case (k, v) => URLEncoder.encode(k, "UTF-8") + "=" + URLEncoder.encode(v, "UTF-8")
      }.mkString("&")
      val key = "yNhI04vHs9<_HWbC`]20u`37=NGLGYY5:0Tg5?y`W<NoJnXWqmjcgZBec@rOxb^G".getBytes
      PlainText(cookieSigner.sign(encoded, key) + "-" + encoded)
    }

    val encodedCookie = encode(sessionData)
    val encrypted = SymmetricCryptoFactory.aesGcmCrypto(cookieKey).encrypt(encodedCookie).value

    s"""mdtp="$encrypted"; Path=/; HTTPOnly"; Path=/; HTTPOnly"""
  }

  def getCookieData(cookie: WSCookie): Map[String, String] = {
    getCookieData(cookie.value)
  }

  def getCookieData(cookieData: String): Map[String, String] = {

    val decrypted = SymmetricCryptoFactory.aesGcmCrypto(cookieKey).decrypt(Crypted(cookieData)).value
    val result = decrypted.split("&")
      .map(_.split("="))
      .map { case Array(k, v) => (k, URLDecoder.decode(v, StandardCharsets.UTF_8.name())) }
      .toMap

    result
  }
}
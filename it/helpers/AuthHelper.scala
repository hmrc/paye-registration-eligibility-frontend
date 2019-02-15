
package helpers

import java.net.{URLDecoder, URLEncoder}
import java.nio.charset.StandardCharsets

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.Application
import play.api.http.HeaderNames
import play.api.libs.crypto.{CryptoConfig, HMACSHA1CookieSigner}
import play.api.libs.json.Json
import play.api.libs.ws.WSCookie
import uk.gov.hmrc.crypto.{CompositeSymmetricCrypto, Crypted, PlainText}
import uk.gov.hmrc.http.SessionKeys

trait AuthHelper extends SessionCookieBaker {

  private[helpers] val defaultUser = "/foo/bar"

  val sessionId = "session-ac4ed3e7-dbc3-4150-9574-40771c4285c1"

  private def cookieData(additionalData: Map[String, String], userId: String = defaultUser): Map[String, String] = {
    Map(
      SessionKeys.sessionId -> sessionId,
      SessionKeys.userId -> userId,
      "token" -> "token",
      "ap" -> "GGW",
      SessionKeys.lastRequestTimestamp -> new java.util.Date().getTime.toString
    ) ++ additionalData
  }

  def getSessionCookie(additionalData: Map[String, String] = Map(), userId: String = defaultUser) = {
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
        case None    => Some(Json.obj("authorise" -> Json.arr(), "retrieve" -> Json.arr()).toString())
      }
    )
  }


  def stubAudits() = {
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
  lazy val cryptIns = new HMACSHA1CookieSigner(app.injector.instanceOf[CryptoConfig])

  val cookieKey = "gvBoGdgzqG1AarzF1LY0zQ=="
  def cookieValue(sessionData: Map[String,String]) = {
    def encode(data: Map[String, String]): PlainText = {
      val encoded = data.map {
        case (k, v) => URLEncoder.encode(k, "UTF-8") + "=" + URLEncoder.encode(v, "UTF-8")
      }.mkString("&")
      val key = "yNhI04vHs9<_HWbC`]20u`37=NGLGYY5:0Tg5?y`W<NoJnXWqmjcgZBec@rOxb^G".getBytes
      PlainText(cryptIns.sign(encoded, key) + "-" + encoded)
    }

    val encodedCookie = encode(sessionData)
    val encrypted = CompositeSymmetricCrypto.aesGCM(cookieKey, Seq()).encrypt(encodedCookie).value

    s"""mdtp="$encrypted"; Path=/; HTTPOnly"; Path=/; HTTPOnly"""
  }

  def getCookieData(cookie: WSCookie): Map[String, String] = {
    getCookieData(cookie.value.get)
  }

  def getCookieData(cookieData: String): Map[String, String] = {

    val decrypted = CompositeSymmetricCrypto.aesGCM(cookieKey, Seq()).decrypt(Crypted(cookieData)).value
    val result = decrypted.split("&")
      .map(_.split("="))
      .map { case Array(k, v) => (k, URLDecoder.decode(v, StandardCharsets.UTF_8.name()))}
      .toMap

    result
  }
}
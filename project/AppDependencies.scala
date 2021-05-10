import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  private val logbackJsonLoggerVersion = "5.1.0"
  private val govukTemplateVersion = "5.66.0-play-26"
  private val playUiVersion = "9.2.0-play-26"
  private val hmrcTestVersion = "3.10.0-play-26"
  private val scalaTestVersion = "3.0.8"
  private val scalaTestPlusPlayVersion = "3.1.3"
  private val pegdownVersion = "1.6.0"
  private val mockitoVersion = "3.9.0"
  private val httpCachingClientVersion = "9.4.0-play-26"
  private val simpleReactivemongoVersion = "8.0.0-play-26"
  private val playConditionalFormMappingVersion = "1.9.0-play-26"
  private val playLanguageVersion = "4.13.0-play-26"
  private val bootstrapVersion = "5.2.0"
  private val wireMockVersion = "2.27.2"
  private val reactivemongoTestVersion = "5.0.0-play-26"
  private val scalacheckVersion = "1.15.3"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "simple-reactivemongo" % simpleReactivemongoVersion,
    "uk.gov.hmrc" %% "logback-json-logger" % logbackJsonLoggerVersion,
    "uk.gov.hmrc" %% "govuk-template" % govukTemplateVersion,
    "uk.gov.hmrc" %% "play-ui" % playUiVersion,
    "uk.gov.hmrc" %% "http-caching-client" % httpCachingClientVersion,
    "uk.gov.hmrc" %% "play-conditional-form-mapping" % playConditionalFormMappingVersion,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-26" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-language" % playLanguageVersion
  )

  private def testDeps(scope: String) = Seq(
    "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
    "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
    "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion % scope,
    "org.pegdown" % "pegdown" % pegdownVersion % scope,
    "org.jsoup" % "jsoup" % "1.10.3" % scope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
    "org.scalacheck" %% "scalacheck" % scalacheckVersion % scope
  )

  object Test {
    def apply(): Seq[ModuleID] = testDeps("test") ++ Seq(
      "org.mockito" % "mockito-core" % mockitoVersion % "test"
    )

  }

  object IntegrationTest {
    def apply(): Seq[ModuleID] = testDeps("it") ++ Seq(
      "com.github.tomakehurst" % "wiremock-jre8" % wireMockVersion % "it",
      "uk.gov.hmrc" %% "reactivemongo-test" % reactivemongoTestVersion % "it"
    )
  }

  def apply(): Seq[ModuleID] = compile ++ Test() ++ IntegrationTest()
}
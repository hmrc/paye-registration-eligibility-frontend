import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  private val logbackJsonLoggerVersion = "5.1.0"
  private val scalaTestVersion = "3.0.8"
  private val scalaTestPlusPlayVersion = "4.0.0"
  private val pegdownVersion = "1.6.0"
  private val mockitoVersion = "3.9.0"
  private val httpCachingClientVersion = "9.5.0-play-28"
  private val simpleReactivemongoVersion = "8.0.0-play-28"
  private val playConditionalFormMappingVersion = "1.9.0-play-28"
  private val playLanguageVersion = "5.1.0-play-28"
  private val bootstrapVersion = "5.16.0"
  private val wireMockVersion = "2.27.2"
  private val reactivemongoTestVersion = "5.0.0-play-28"
  private val scalacheckVersion = "1.15.3"
  private val hmrcFrontendVersion = "3.21.0-play-28"

  private def testDependencies(scope: String): Seq[ModuleID] = Seq(
    "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
    "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion % scope,
    "org.pegdown" % "pegdown" % pegdownVersion % scope,
    "org.jsoup" % "jsoup" % "1.10.3" % scope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
    "org.scalacheck" %% "scalacheck" % scalacheckVersion % scope
  )

  val appDependencies = Seq(
    ws,
    "uk.gov.hmrc" %% "simple-reactivemongo" % simpleReactivemongoVersion,
    "uk.gov.hmrc" %% "logback-json-logger" % logbackJsonLoggerVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc" % hmrcFrontendVersion,
    "uk.gov.hmrc" %% "http-caching-client" % httpCachingClientVersion,
    "uk.gov.hmrc" %% "play-conditional-form-mapping" % playConditionalFormMappingVersion,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-language" % playLanguageVersion
  )

  val unitTestDependencies: Seq[ModuleID] = testDependencies("test") ++ Seq("org.mockito" % "mockito-core" % mockitoVersion % "test")

  val itDependencies: Seq[ModuleID] = testDependencies("it") ++ Seq(
    "com.github.tomakehurst" % "wiremock-jre8" % wireMockVersion % "it",
    "uk.gov.hmrc" %% "reactivemongo-test" % reactivemongoTestVersion % "it"
  )

  def apply(): Seq[ModuleID] = appDependencies ++ unitTestDependencies ++ itDependencies
}
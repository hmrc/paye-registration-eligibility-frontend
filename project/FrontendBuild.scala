import sbt._

object FrontendBuild extends Build with MicroService {

  val appName = "paye-registration-eligibility-frontend"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {

  import play.core.PlayVersion
  import play.sbt.PlayImport._

  private val logbackJsonLoggerVersion = "4.6.0"
  private val govukTemplateVersion = "5.48.0-play-26"
  private val playUiVersion = "8.7.0-play-26"
  private val scalaTestVersion = "3.0.0"
  private val scalaTestPlusPlayVersion = "3.1.2"
  private val pegdownVersion = "1.6.0"
  private val httpCachingClientVersion = "9.0.0-play-26"
  private val simpleReactivemongoVersion = "7.30.0-play-26"
  private val playConditionalFormMappingVersion = "1.2.0-play-26"
  private val playLanguageVersion = "4.2.0-play-26"
  private val bootstrapVersion = "1.8.0"
  private val scalacheckVersion = "1.14.2"
  private val wireMockVersion = "2.25.1"
  private val reactivemongoTestVersion = "4.21.0-play-26"
  private val mockitoCoreVersion = "2.13.0"
  private val akkaVersion = "2.5.23"

  val compile = Seq(
    ws,
    "com.enragedginger" %% "akka-quartz-scheduler" % "1.8.1-akka-2.5.x",
    "uk.gov.hmrc" %% "simple-reactivemongo" % simpleReactivemongoVersion,
    "uk.gov.hmrc" %% "logback-json-logger" % logbackJsonLoggerVersion,
    "uk.gov.hmrc" %% "govuk-template" % govukTemplateVersion,
    "uk.gov.hmrc" %% "play-ui" % playUiVersion,
    "uk.gov.hmrc" %% "http-caching-client" % httpCachingClientVersion,
    "uk.gov.hmrc" %% "play-conditional-form-mapping" % playConditionalFormMappingVersion,
    "uk.gov.hmrc" %% "bootstrap-play-26" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-language" % playLanguageVersion,
    "com.typesafe.play" %% "play-json-joda" % "2.6.10",
    "com.typesafe.akka" %% "akka-stream" % akkaVersion force(),
    "com.typesafe.akka" %% "akka-protobuf" % akkaVersion force(),
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion force(),
    "com.typesafe.akka" %% "akka-actor" % akkaVersion force()
  )

  object Dep {
    def testDeps(scope: String) = {
      Seq(
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "org.jsoup" % "jsoup" % "1.10.3" % scope,
        "org.mockito" % "mockito-core" % mockitoCoreVersion % scope,
        "org.scalacheck" %% "scalacheck" % scalacheckVersion % scope,
        "uk.gov.hmrc" %% "bootstrap-play-26" % bootstrapVersion % scope
      )
    }
  }


  object Test {
    def apply(): Seq[ModuleID] = Dep.testDeps("test")
  }


  object IntegrationTest {
    def apply() = Dep.testDeps("it") ++ Seq(
      "com.github.tomakehurst" % "wiremock-jre8" % wireMockVersion % "it",
      "uk.gov.hmrc" %% "reactivemongo-test" % reactivemongoTestVersion % "it",
      "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion % "it"
    )
  }

  def apply() = compile ++ Test() ++ IntegrationTest()
}

import sbt._

object FrontendBuild extends Build with MicroService {

  val appName = "paye-registration-eligibility-frontend"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {
  import play.core.PlayVersion
  import play.sbt.PlayImport._

  private val logbackJsonLoggerVersion = "3.1.0"
  private val govukTemplateVersion = "5.36.0-play-25"
  private val playUiVersion = "8.8.0-play-25"
  private val scalaTestVersion = "3.0.8"
  private val scalaTestPlusPlayVersion = "2.0.1"
  private val pegdownVersion = "1.6.0"
  private val httpCachingClientVersion = "9.0.0-play-25"
  private val simpleReactivemongoVersion = "7.23.0-play-25"
  private val playConditionalFormMappingVersion = "1.2.0-play-25"
  private val playLanguageVersion = "4.2.0-play-25"
  private val bootstrapVersion = "5.1.0"
  private val scalacheckVersion = "1.13.4"
  private val wireMockVersion = "2.6.0"
  private val reactivemongoTestVersion = "4.16.0-play-25"
  private val mockitoCoreVersion = "2.13.0"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "simple-reactivemongo"           % simpleReactivemongoVersion,
    "uk.gov.hmrc" %% "logback-json-logger"            % logbackJsonLoggerVersion,
    "uk.gov.hmrc" %% "govuk-template"                 % govukTemplateVersion,
    "uk.gov.hmrc" %% "play-ui"                        % playUiVersion,
    "uk.gov.hmrc" %% "http-caching-client"            % httpCachingClientVersion,
    "uk.gov.hmrc" %% "play-conditional-form-mapping"  % playConditionalFormMappingVersion,
    "uk.gov.hmrc" %% "bootstrap-play-25"              % bootstrapVersion,
    "uk.gov.hmrc" %% "play-language"                  % playLanguageVersion
  )

object Dep {
  def testDeps(scope: String) = {
    Seq(
    "org.scalatest"           %% "scalatest"          % scalaTestVersion          % scope,
    "org.scalatestplus.play"  %% "scalatestplus-play" % scalaTestPlusPlayVersion  % scope,
    "org.pegdown"             % "pegdown"             % pegdownVersion            % scope,
    "org.jsoup"               % "jsoup"               % "1.10.3"                  % scope,
    "com.typesafe.play"       %% "play-test"          % PlayVersion.current       % scope,
    "org.mockito"             %  "mockito-core"       % mockitoCoreVersion        % scope,
    "org.scalacheck"          %% "scalacheck"         % scalacheckVersion         % scope
    )
  }
}


  object Test {
  def apply():Seq[ModuleID] = Dep.testDeps("test")
  }


  object IntegrationTest {
    def apply() = Dep.testDeps("it") ++ Seq(
      "com.github.tomakehurst" % "wiremock" % wireMockVersion % "it",
      "uk.gov.hmrc"           %% "reactivemongo-test" % reactivemongoTestVersion % "it",
      "org.scalatestplus.play"  %% "scalatestplus-play" % scalaTestPlusPlayVersion % "it"
    )
  }

  def apply() = compile ++ Test() ++ IntegrationTest()
}

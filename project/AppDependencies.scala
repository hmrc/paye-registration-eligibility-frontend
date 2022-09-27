import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  private val playVersion = "-play-28"

  private val scalaTestVersion = "3.2.12"
  private val scalaTestPlusPlayVersion = "5.1.0"
  private val httpCachingClientVersion = s"9.6.0$playVersion"
  private val playConditionalFormMappingVersion = s"1.11.0$playVersion"
  private val bootstrapVersion = "5.16.0"
  private val wireMockVersion = "2.27.2"
  private val hmrcFrontendVersion = s"3.28.0$playVersion"
  private val hmrcMongoVersion = "0.73.0"
  private val flexmarkAllVersion = "0.62.2"

  val appDependencies = Seq(
    ws,
    "uk.gov.hmrc.mongo" %% s"hmrc-mongo$playVersion"           % hmrcMongoVersion,
    "uk.gov.hmrc"       %%  "play-frontend-hmrc"               % hmrcFrontendVersion,
    "uk.gov.hmrc"       %%  "http-caching-client"              % httpCachingClientVersion,
    "uk.gov.hmrc"       %%  "play-conditional-form-mapping"    % playConditionalFormMappingVersion,
    "uk.gov.hmrc"       %% s"bootstrap-frontend$playVersion"   % bootstrapVersion
  )

  private def testDependencies(scope: String): Seq[ModuleID] = Seq(
    "org.scalatest"             %% "scalatest"              % scalaTestVersion          % scope,
    "org.scalatestplus.play"    %% "scalatestplus-play"     % scalaTestPlusPlayVersion  % scope,
    "org.jsoup"                 %  "jsoup"                  % "1.10.3"                  % scope,
    "com.typesafe.play"         %% "play-test"              % PlayVersion.current       % scope,
    "org.scalatestplus"         %% "scalacheck-1-16"        % s"$scalaTestVersion.0"    % scope,
    "com.vladsch.flexmark"      %  "flexmark-all"           % flexmarkAllVersion        % scope
  )

  val unitTestDependencies: Seq[ModuleID] = testDependencies("test") ++ Seq(
    "org.scalatestplus"         %% "mockito-4-5"            % s"$scalaTestVersion.0"    % "test"
  )

  val itDependencies: Seq[ModuleID] = testDependencies("it") ++ Seq(
    "com.github.tomakehurst"    %   "wiremock-jre8"               % wireMockVersion     % "it",
    "uk.gov.hmrc.mongo"         %% s"hmrc-mongo-test$playVersion" % hmrcMongoVersion    % "it"
  )

  def apply(): Seq[ModuleID] = appDependencies ++ unitTestDependencies ++ itDependencies
}
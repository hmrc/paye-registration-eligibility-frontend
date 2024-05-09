import play.core.PlayVersion
import play.sbt.PlayImport.*
import sbt.*

object AppDependencies {

  private val playVersion = "-play-30"

  private val scalaTestVersion = "3.2.18"
  private val scalaTestPlusPlayVersion = "5.1.0"
  private val httpCachingClientVersion = "11.2.0"
  private val playConditionalFormMappingVersion = "2.0.0"
  private val bootstrapVersion = "8.5.0"
  private val wireMockVersion = "2.35.0"
  private val hmrcFrontendVersion = "9.10.0"
  private val hmrcMongoVersion = "1.9.0"
  private val flexmarkAllVersion = "0.64.8"

  val appDependencies: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc.mongo"   %% s"hmrc-mongo$playVersion"                        % hmrcMongoVersion,
    "uk.gov.hmrc"         %% s"play-frontend-hmrc$playVersion"                % hmrcFrontendVersion,
    "uk.gov.hmrc"         %% s"http-caching-client$playVersion"               % httpCachingClientVersion,
    "uk.gov.hmrc"         %% s"play-conditional-form-mapping$playVersion"     % playConditionalFormMappingVersion,
    "uk.gov.hmrc"         %% s"bootstrap-frontend$playVersion"                % bootstrapVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"               %% s"bootstrap-test$playVersion"    % bootstrapVersion          % Test,
    "org.scalatest"             %%  "scalatest"                     % scalaTestVersion          % Test,
    "org.scalatestplus.play"    %%  "scalatestplus-play"            % scalaTestPlusPlayVersion  % Test,
    "org.jsoup"                 %   "jsoup"                         % "1.17.2"                  % Test,
    "org.playframework"         %%  "play-test"                     % PlayVersion.current       % Test,
    "org.scalatestplus"         %%  "scalacheck-1-17"               % s"$scalaTestVersion.0"    % Test,
    "com.vladsch.flexmark"      %   "flexmark-all"                  % flexmarkAllVersion        % Test,
    "org.scalatestplus"         %%  "mockito-4-5"                   % "3.2.12.0"                % Test,
    "org.wiremock"              % "wiremock-standalone"             % "3.5.4"                   % Test,
    "uk.gov.hmrc.mongo"         %% s"hmrc-mongo-test$playVersion"   % hmrcMongoVersion          % Test
  )

  def apply(): Seq[ModuleID] = appDependencies ++ test
}
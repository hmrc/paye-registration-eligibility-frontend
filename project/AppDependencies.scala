import play.core.PlayVersion
import play.sbt.PlayImport.*
import sbt.*

object AppDependencies {

  private val playVersion = "-play-30"

  private val scalaTestVersion = "3.2.20"
  private val scalaTestPlusPlayVersion = "7.0.2"
  private val playConditionalFormMappingVersion = "3.5.0"
  private val bootstrapVersion = "10.7.1"
  private val wireMockVersion = "2.35.0"
  private val hmrcFrontendVersion = "13.11.0"
  private val hmrcMongoVersion = "2.13.0"
  private val flexmarkAllVersion = "0.64.8"

  val appDependencies: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc.mongo"   %% s"hmrc-mongo$playVersion"                        % hmrcMongoVersion,
    "uk.gov.hmrc"         %% s"play-frontend-hmrc$playVersion"                % hmrcFrontendVersion,
    "uk.gov.hmrc"         %% s"play-conditional-form-mapping$playVersion"     % playConditionalFormMappingVersion,
    "uk.gov.hmrc"         %% s"bootstrap-frontend$playVersion"                % bootstrapVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"               %% s"bootstrap-test$playVersion"    % bootstrapVersion          % Test,
    "org.scalatestplus.play"    %%  "scalatestplus-play"            % scalaTestPlusPlayVersion  % Test,
    "org.jsoup"                 %   "jsoup"                         % "1.23.2"                  % Test,
    "org.playframework"         %%  "play-test"                     % PlayVersion.current       % Test,
    "org.scalatestplus"         %%  "scalacheck-1-19"               % "3.2.20.0"                  % Test,
    "com.vladsch.flexmark"      %   "flexmark-all"                  % flexmarkAllVersion        % Test,
    "org.wiremock"              % "wiremock-standalone"             % "3.13.2"                  % Test,
    "uk.gov.hmrc.mongo"         %% s"hmrc-mongo-test$playVersion"   % hmrcMongoVersion          % Test
  )

  def apply(): Seq[ModuleID] = appDependencies ++ test
}
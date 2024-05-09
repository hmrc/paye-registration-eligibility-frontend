import play.sbt.routes.RoutesKeys
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, integrationTestSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName: String = "paye-registration-eligibility-frontend"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(Seq(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtDistributablesPlugin) *)
  .settings(RoutesKeys.routesImport ++= Seq("models._"))
  .settings(
    ScoverageKeys.coverageExcludedFiles :=
      Seq(
        "<empty>",
        "Reverse.*",
        ".*config.*",
        ".*filters.*",
        ".*handlers.*",
        ".*components.*",
        ".*identifiers.*",
        ".*BuildInfo.*",
        ".*javascript.*",
        ".*FrontendAuditConnector.*",
        ".*Routes.*",
        ".*GuiceInjector",
        ".*ControllerConfiguration",
        ".*LanguageSwitchController",
        ".*controllers.tests.*"
      ).mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    Test / parallelExecution := false
  )
  .settings(scalaSettings *)
  .settings(publishingSettings *)
  .settings(defaultSettings() *)
  .settings(majorVersion := 0)
  .settings(
    scalacOptions ++= Seq("-feature", "-Xlint:-unused"),
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings) *)
  .settings(integrationTestSettings())

scalaVersion := "2.13.13"

Test / javaOptions += "-Dlogger.resource=logback-test.xml"
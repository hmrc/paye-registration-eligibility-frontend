import play.sbt.routes.RoutesKeys
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings}

val appName: String = "paye-registration-eligibility-frontend"
ThisBuild / majorVersion := 1
ThisBuild / scalaVersion := "2.13.13"

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
  .settings(defaultSettings() *)
  .settings(majorVersion := 0)
  .settings(
    scalacOptions ++= Seq("-feature", "-Xlint:-unused"),
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true
  )

lazy val it = project.in(file("it"))
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(DefaultBuildSettings.itSettings(true))
  .settings(
    libraryDependencies ++= AppDependencies(),
    addTestReportOption(Test, "int-test-reports")
  )

Test / javaOptions += "-Dlogger.resource=logback-test.xml"
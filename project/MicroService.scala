import sbt.Keys._
import sbt.Tests.{Group, SubProcess}
import sbt._
import scoverage.ScoverageKeys
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import com.typesafe.sbt.web.Import._
import net.ground5hark.sbt.concat.Import._
import com.typesafe.sbt.uglify.Import._
import com.typesafe.sbt.digest.Import._
import play.sbt.PlayImport.PlayKeys
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning
import uk.gov.hmrc.SbtArtifactory

trait MicroService {

  import uk.gov.hmrc._
  import DefaultBuildSettings._
  import uk.gov.hmrc.{SbtBuildInfo, ShellPrompt, SbtAutoBuildPlugin}
  import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
  import uk.gov.hmrc.versioning.SbtGitVersioning
  import play.sbt.routes.RoutesKeys.routesGenerator
  import play.sbt.routes.RoutesKeys

  import TestPhases._

  val appName: String

  lazy val appDependencies : Seq[ModuleID] = ???
  lazy val plugins : Seq[Plugins] = Seq.empty
  lazy val playSettings : Seq[Setting[_]] = Seq.empty

  lazy val microservice = Project(appName, file("."))
    .enablePlugins(Seq(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory) ++ plugins : _*)
    .settings(playSettings : _*)
    .settings(PlayKeys.playDefaultPort := 9877)
    .settings(RoutesKeys.routesImport ++= Seq("models._"))
    .settings(
      ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*filters.*;.*handlers.*;.*components.*;.*models.*;.*repositories.*;" +
        ".*BuildInfo.*;.*javascript.*;.*FrontendAuditConnector.*;.*Routes.*;.*GuiceInjector;.*DataCacheConnector;" +
        ".*ControllerConfiguration;.*LanguageSwitchController",
      ScoverageKeys.coverageMinimum := 90,
      ScoverageKeys.coverageFailOnMinimum := false,
      ScoverageKeys.coverageHighlighting := true,
      parallelExecution in Test := false
    )
    .settings(scalaSettings: _*)
    .settings(publishingSettings: _*)
    .settings(majorVersion := 0)
    .settings(defaultSettings(): _*)
    .settings(
      scalacOptions ++= Seq("-Xfatal-warnings", "-feature"),
      libraryDependencies ++= appDependencies,
      retrieveManaged := true,
      evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
    )
    .configs(IntegrationTest)
    .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
    .settings(resolvers ++= Seq(
        Resolver.bintrayRepo("hmrc", "releases"),
        Resolver.jcenterRepo,
        Resolver.bintrayRepo("emueller", "maven")
      ))
    .settings(
      // concatenate js
      Concat.groups := Seq(
        "javascripts/payeregistrationeligibilityfrontend-app.js" -> group(Seq("javascripts/show-hide-content.js", "javascripts/payeregistrationeligibilityfrontend.js"))
      ),
      // prevent removal of unused code which generates warning errors due to use of third-party libs
      UglifyKeys.compressOptions := Seq("unused=false", "dead_code=false"),
      pipelineStages := Seq(digest),
      // below line required to force asset pipeline to operate in dev rather than only prod
      pipelineStages in Assets := Seq(concat,uglify),
      // only compress files generated by concat
      includeFilter in uglify := GlobFilter("payeregistrationeligibilityfrontend-*.js")
    )
}

private object TestPhases {

  def oneForkedJvmPerTest(tests: Seq[TestDefinition]) =
    tests map {
      test => new Group(test.name, Seq(test), SubProcess(ForkOptions(runJVMOptions = Seq("-Dtest.name=" + test.name))))
    }
}

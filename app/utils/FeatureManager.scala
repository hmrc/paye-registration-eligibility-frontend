/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utils

import javax.inject.Inject

sealed trait FeatureSwitch {

  def name: String

  def value: String

  def enabled: Boolean
}

class FeatureSwitchManager extends FeatureManager

case class ValueSetFeatureSwitch(name: String, setValue: String) extends FeatureSwitch {
  override def value: String = setValue

  override def enabled: Boolean = setValue.matches(DateUtil.datePatternRegex)
}

case class BooleanFeatureSwitch(name: String, setValue: Boolean) extends FeatureSwitch {
  override def value: String = setValue.toString

  override def enabled: Boolean = setValue
}

trait FeatureManager {

  val datePatternRegex: String = """([12]\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\d|3[01]))"""

  private[utils] def systemPropertyName(name: String) = s"feature.$name"

  private[utils] def getProperty[T](name: String, defaultValue: T): FeatureSwitch = {
    val value = sys.props.get(systemPropertyName(name))

    value match {
      case Some("true") => BooleanFeatureSwitch(name, setValue = true)
      case Some("false") => BooleanFeatureSwitch(name, setValue = false)
      case Some(date) if date.matches(DateUtil.datePatternRegex) => ValueSetFeatureSwitch(name, date)
      case _ if defaultValue.isInstanceOf[String] => ValueSetFeatureSwitch(name, "time-clear")
      case _ if defaultValue.isInstanceOf[Boolean] => BooleanFeatureSwitch(name, setValue = false)
    }
  }

  private[utils] def setProperty[T](name: String, value: String, default: T): FeatureSwitch = {
    sys.props += ((systemPropertyName(name), value))
    getProperty[T](name, default)
  }

  def enableORDisable(fs: FeatureSwitch): FeatureSwitch = setProperty(fs.name, fs.value, false)

  def setSystemDate(fs: FeatureSwitch): FeatureSwitch = setProperty(fs.name, fs.value, "time-clear")

  def clearSystemDate(fs: FeatureSwitch): FeatureSwitch = setProperty(fs.name, "", "time-clear")
}

class PREFEFeatureSwitch @Inject()(injManager: FeatureSwitchManager) extends PREFEFeatureSwitches {
  val companyRegistration = "companyRegistration"
  val isWelsh = "isWelsh"
  override val manager: FeatureManager = injManager
  override val setSystemDate = "system-date"
}

trait PREFEFeatureSwitches {
  val setSystemDate: String
  protected val companyRegistration: String
  protected val isWelsh: String
  val manager: FeatureManager

  private def systemDate: FeatureSwitch = manager.getProperty[String](setSystemDate, "time-clear")

  def companyReg: FeatureSwitch = manager.getProperty[Boolean](companyRegistration, false)

  def isWelshEnabled: FeatureSwitch = manager.getProperty[Boolean](isWelsh, false)

  def apply(name: String): Option[FeatureSwitch] = name match {
    case `setSystemDate` => Some(systemDate)
    case `companyRegistration` => Some(companyReg)
    case `isWelsh` => Some(isWelshEnabled)
    case _ => None
  }
}
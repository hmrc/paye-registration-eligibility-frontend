/*
 * Copyright 2018 HM Revenue & Customs
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
}

class FeatureSwitchManager extends FeatureManager

case class ValueSetFeatureSwitch(name: String, setValue: String) extends FeatureSwitch {
  override def value: String = setValue
}

trait FeatureManager {

  private[utils] def systemPropertyName(name: String) = s"feature.$name"

  private[utils] def getProperty(name: String): FeatureSwitch = {
    val value = sys.props.get(systemPropertyName(name))

    value match {
      case Some(date) if date.matches(DateUtil.datePatternRegex) => ValueSetFeatureSwitch(name, date)
      case _                                                     => ValueSetFeatureSwitch(name, "time-clear")
    }
  }

  private[utils] def setProperty(name: String, value: String): FeatureSwitch = {
    sys.props += ((systemPropertyName(name), value))
    getProperty(name)
  }


  def setSystemDate(fs: FeatureSwitch): FeatureSwitch   = setProperty(fs.name, fs.value)
  def clearSystemDate(fs: FeatureSwitch): FeatureSwitch = setProperty(fs.name, "")
}

class PREFEFeatureSwitch @Inject()(injManager: FeatureSwitchManager) extends PREFEFeatureSwitches {
  override val manager: FeatureManager = injManager
  override val setSystemDate           = "system-date"
}

trait PREFEFeatureSwitches {
  val setSystemDate: String
  val manager: FeatureManager

  def systemDate: FeatureSwitch   = manager.getProperty(setSystemDate)

  def apply(name: String): Option[FeatureSwitch] = name match {
    case `setSystemDate` => Some(systemDate)
    case _               => None
  }
}


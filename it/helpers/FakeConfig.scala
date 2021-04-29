/*
 * Copyright 2021 HM Revenue & Customs
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

package helpers

import uk.gov.hmrc.mongo.MongoSpecSupport

trait FakeConfig {
  self: MongoSpecSupport =>
  val mockHost: String
  val mockPort: Int
  lazy val mockUrl = s"http://$mockHost:$mockPort"

  def fakeConfig(extraConfig: (String,String)*) = Map(
    "play.filters.csrf.header.bypassHeaders.X-Requested-With" -> "*",
    "play.filters.csrf.header.bypassHeaders.Csrf-Token" -> "nocheck",
    "auditing.consumer.baseUri.host" -> s"$mockHost",
    "auditing.consumer.baseUri.port" -> s"$mockPort",
    "mongodb.uri" -> s"$mongoUri",
    "microservice.services.auth.host" -> s"$mockHost",
    "microservice.services.auth.port" -> s"$mockPort",
    "microservice.services.company-auth.host" -> s"$mockHost",
    "microservice.services.company-auth.port" -> s"$mockPort"
  ) ++ extraConfig
}
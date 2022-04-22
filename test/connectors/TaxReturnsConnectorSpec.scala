/*
 * Copyright 2022 HM Revenue & Customs
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

package connectors

import base.utils.ConnectorISpec
import com.github.tomakehurst.wiremock.client.WireMock
import controllers.helpers.TaxReturnBuilder
import org.scalatest.EitherValues
import org.scalatest.concurrent.ScalaFutures

class TaxReturnsConnectorSpec
  extends ConnectorISpec with ScalaFutures with EitherValues with TaxReturnBuilder {

  lazy val connector: TaxReturnsConnector = app.injector.instanceOf[TaxReturnsConnector]

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    WireMock.configureFor(wireHost, wirePort)
    wireMockServer.start()
  }

  override protected def afterAll(): Unit = {
    wireMockServer.stop()
    super.afterAll()
  }

  "create tax return" should {
    "return success response" when {
      "valid request send" in {
        // TODO - we need to look at adding specs here. What do we expect back from this endpoint? What do we want to do with it?
      }
    }
  }
}
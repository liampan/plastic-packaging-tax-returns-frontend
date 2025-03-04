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

package uk.gov.hmrc.plasticpackagingtax.returns.controllers.actions

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration

class PptReferenceAllowedListProviderSpec extends AnyWordSpec with Matchers with MockitoSugar {

  "PptReferenceAllowedListProvider" should {

    "load correctly from configuration" in {

      val config   = Configuration("allowedList.pptReference.0" -> "1234")
      val provider = new PptReferenceAllowedListProvider(config)
      provider.get() mustBe a[PptReferenceAllowedList]
    }

    "trim spaces during loading" in {

      val config   = Configuration("allowedList.pptReference.0" -> " 1234 ")
      val provider = new PptReferenceAllowedListProvider(config)
      provider.get().isAllowed("1234") mustBe true
    }

    "allow empty list" in {

      val config   = Configuration("allowedList.pptReference.0" -> "")
      val provider = new PptReferenceAllowedListProvider(config)
      provider.get() mustBe a[PptReferenceAllowedList]
    }

    "throw exception when there is not configuration key" in {

      val provider = new PptReferenceAllowedListProvider(Configuration.empty)
      an[Exception] mustBe thrownBy {
        provider.get()
      }
    }
  }
}

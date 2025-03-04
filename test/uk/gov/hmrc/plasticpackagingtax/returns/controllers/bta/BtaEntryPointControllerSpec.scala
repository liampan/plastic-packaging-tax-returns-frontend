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

package uk.gov.hmrc.plasticpackagingtax.returns.controllers.bta

import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.test.Helpers.redirectLocation
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.ControllerSpec
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.returns.{routes => returnRoutes}
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

class BtaEntryPointControllerSpec extends ControllerSpec {

  private val controller = new BtaEntryPointController(mcc = stubMessagesControllerComponents())

  "The BTA Entry Point Controller" should {
    "redirect to the create tax return start page" in {
      redirectLocation(controller.startReturn()(getRequest())) mustBe Some(
        returnRoutes.StartDateReturnController.displayPage().url
      )
    }
    "redirect to the submitted returns page" in {
      redirectLocation(controller.submittedReturns()(getRequest())) mustBe Some(
        returnRoutes.SubmittedReturnsController.displayPage().url
      )
    }
  }
}

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

package uk.gov.hmrc.plasticpackagingtax.returns.base.unit

import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.`given`
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.plasticpackagingtax.returns.connectors.ObligationsConnector
import uk.gov.hmrc.plasticpackagingtax.returns.models.obligations.{Obligation, PPTObligations}
import uk.gov.hmrc.plasticpackagingtax.returns.models.request.JourneyAction

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

trait MockJourneyAction
    extends MockTaxReturnsConnector with MockObligationsConnector with MockAuditorConnector
    with BeforeAndAfterEach with MockitoSugar {
  self: MockitoSugar with Suite =>

  val mockJourneyAction: JourneyAction =
    new JourneyAction(mockTaxReturnsConnector, mockAuditor, mockObligationsConnector)(
      ExecutionContext.global
    )

  def mockGetObligation() =
    given(mockObligationsConnector.get(any())(any[HeaderCarrier]))
      .willReturn(
        Future.successful(
          PPTObligations(None,
                         Some(Obligation(LocalDate.now(), LocalDate.now(), LocalDate.now(), "str")),
                         0,
                         false,
                         false
          )
        )
      )

  override protected def beforeEach() {
    super.beforeEach()
    given(mockTaxReturnsConnector.find(any())(any())).willReturn(
      Future.successful(Right(Option(aTaxReturn(withId("001")))))
    )
    mockGetObligation()
  }

}

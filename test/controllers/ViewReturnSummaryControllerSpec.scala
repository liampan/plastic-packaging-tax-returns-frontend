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

package controllers

import base.SpecBase
import connectors.{CacheConnector, TaxReturnsConnector}
import models.returns.{IdDetails, ReturnDisplayApi, ReturnDisplayChargeDetails, ReturnDisplayDetails}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.checkAnswers.ViewReturnSummaryViewModel
import views.html.ViewReturnSummaryView

import scala.concurrent.Future

class ViewReturnSummaryControllerSpec extends SpecBase with MockitoSugar {

  private val mockConnector = mock[TaxReturnsConnector]

  "ViewReturnSummary Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          inject.bind[TaxReturnsConnector].toInstance(mockConnector),
          inject.bind[CacheConnector].toInstance(cacheConnector)
        )
        .build()

      val charge = ReturnDisplayChargeDetails(
        periodFrom = "2022-04-01",
        periodTo ="2022-06-30",
        periodKey = "22AC",
        chargeReference = Some("pan"),
        receiptDate = "2022-06-31",
        returnType = "TYPE"
      )

      val returnDisplayDetails = ReturnDisplayDetails(1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
      val submittedReturn      = ReturnDisplayApi("", IdDetails("", ""), Some(charge), returnDisplayDetails)

      running(application) {
        val viewModel = ViewReturnSummaryViewModel(submittedReturn)

        when(mockConnector.get(any(), any())(any())).thenReturn(
          Future.successful(Right(submittedReturn))
        )

        when(cacheConnector.set(any(), any())(any())).thenReturn(Future.successful(mockResponse))

        val request = FakeRequest(GET, routes.ViewReturnSummaryController.onPageLoad("00xx").url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ViewReturnSummaryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view("April to June 2022", viewModel)(
          request,
          messages(application)
        ).toString

        // Check it capitalised the period key
        verify(mockConnector).get(any(), ArgumentMatchers.eq("00XX"))(any())
      }
    }

  }
}

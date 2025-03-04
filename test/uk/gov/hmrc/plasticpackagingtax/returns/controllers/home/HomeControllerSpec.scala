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

package uk.gov.hmrc.plasticpackagingtax.returns.controllers.home

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => mockitoEq}
import org.mockito.Mockito.{reset, verify, verifyNoInteractions, when}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.http.Status.OK
import play.api.test.Helpers.{await, redirectLocation, status}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.plasticpackagingtax.returns.base.PptTestData.{
  createSubscriptionDisplayResponse,
  ukLimitedCompanySubscription
}
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.ControllerSpec
import uk.gov.hmrc.plasticpackagingtax.returns.config.Features
import uk.gov.hmrc.plasticpackagingtax.returns.connectors.FinancialsConnector
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.deregistration.{
  routes => deregistrationRoutes
}
import uk.gov.hmrc.plasticpackagingtax.returns.models.{EisError, EisFailure}
import uk.gov.hmrc.plasticpackagingtax.returns.models.financials.PPTFinancials
import uk.gov.hmrc.plasticpackagingtax.returns.models.obligations.PPTObligations
import uk.gov.hmrc.plasticpackagingtax.returns.models.subscription.subscriptionDisplay.SubscriptionDisplayResponse
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.home.home_page
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

import scala.concurrent.Future

class HomeControllerSpec extends ControllerSpec {

  private val mcc  = stubMessagesControllerComponents()
  private val page = mock[home_page]

  private val mockFinancialsConnector = mock[FinancialsConnector]

  private val controller = new HomeController(authenticate = mockAuthAction,
                                              subscriptionConnector = mockSubscriptionConnector,
                                              financialsConnector = mockFinancialsConnector,
                                              obligationsConnector = mockObligationsConnector,
                                              appConfig = config,
                                              mcc = mcc,
                                              homePage = page
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(
      page.apply(any(), any[SubscriptionDisplayResponse], any(), any(), any(), any())(any(), any())
    ).thenReturn(HtmlFormat.empty)
    when(mockFinancialsConnector.getPaymentStatement(any[String])(any())).thenReturn(
      Future.successful(PPTFinancials(None, None, None))
    )
  }

  override protected def afterEach(): Unit = {
    reset(page,
          mockFinancialsConnector,
          mockObligationsConnector,
          config,
          mockSubscriptionConnector
    )
    super.afterEach()
  }

  "HomePage Controller" should {

    "return 200" when {

      "use is authorised and display page method is invoked" in {
        authorizedUser()

        val subscription = createSubscriptionDisplayResponse(ukLimitedCompanySubscription)
        mockGetSubscription(subscription)

        val result = controller.displayPage()(getRequest())

        status(result) mustBe OK
      }
    }

    "redirect to the deregistered page" when {
      "get subscription returns a 404 (NOT_FOUND) and EisFailure body confirming this" in {
        authorizedUser()
        mockGetSubscriptionFailure(
          EisFailure(
            Seq(
              EisError(
                "NO_DATA_FOUND",
                "The remote endpoint has indicated that the requested resource could not be found."
              )
            ),
            404
          )
        )

        val result = controller.displayPage()(getRequest())

        redirectLocation(result) mustBe Some(
          deregistrationRoutes.DeregisteredController.displayPage().url
        )
      }
    }

    "avoid calling Obligation Api" when {
      "return is not enabled" in {
        authorizedUser()
        setUpMocks()
        when(config.isFeatureEnabled(mockitoEq(Features.returnsEnabled))).thenReturn(false)

        await(controller.displayPage()(getRequest()))

        verifyNoInteractions(mockObligationsConnector)
        verifyResults(PPTObligations(None, None, 0, false, false))
      }
    }

    "call Obligation API" when {
      "return feature flag is enabled" in {
        val expectedObligation = PPTObligations(None, None, 1, true, true)
        authorizedUser()
        setUpMocks(expectedObligation)
        when(config.isFeatureEnabled(mockitoEq(Features.returnsEnabled))).thenReturn(true)

        await(controller.displayPage()(getRequest()))

        verify(mockObligationsConnector).get(any[String])(any())
        verifyResults(expectedObligation)
      }
    }

    "avoid to call the financial API" when {
      "return feature flag is not enabled" in {
        authorizedUser()
        setUpMocks()
        when(config.isFeatureEnabled(mockitoEq(Features.paymentsEnabled))).thenReturn(false)

        await(controller.displayPage()(getRequest()))

        verifyNoInteractions(mockFinancialsConnector)
        val captor: ArgumentCaptor[Option[String]] =
          ArgumentCaptor.forClass(classOf[Option[String]])
        verify(page).apply(any(), any(), any(), captor.capture(), any(), any())(any(), any())

        captor.getValue.get mustBe "account.homePage.card.payments.nothingOutstanding"
      }
    }

    "call Financial API" when {
      "return feature is enabled" in {
        authorizedUser()
        setUpMocks()
        when(config.isFeatureEnabled(mockitoEq(Features.paymentsEnabled))).thenReturn(true)

        await(controller.displayPage()(getRequest()))

        verify(mockFinancialsConnector).getPaymentStatement(any[String])(any())
      }
    }

    "return an error" when {

      "user is not authorised" in {
        unAuthorizedUser()
        val result = controller.displayPage()(getRequest())

        intercept[RuntimeException](status(result))
      }

      "get subscription returns a 404 (NOT_FOUND) but no confirming EisFailure in the body" in {
        authorizedUser()
        mockGetSubscriptionFailure(
          EisFailure(Seq(EisError("INTERNAL_SERVER_ERROR", "Something's gone BANG!")), 404)
        )

        val result = controller.displayPage()(getRequest())

        intercept[RuntimeException](status(result))
      }

      "get subscription returns a failure other than 404 (NOT_FOUND)" in {
        authorizedUser()
        mockGetSubscriptionFailure(
          EisFailure(Seq(EisError("INTERNAL_SERVER_ERROR", "Something's gone BANG!")), 500)
        )

        val result = controller.displayPage()(getRequest())

        intercept[RuntimeException](status(result))
      }

    }
  }

  private def setUpMocks(obligation: PPTObligations = createDefaultPPTObligation) = {
    val subscription = createSubscriptionDisplayResponse(ukLimitedCompanySubscription)
    mockGetSubscription(subscription)

    when(mockFinancialsConnector.getPaymentStatement(any[String])(any())).thenReturn(
      Future.successful(PPTFinancials(None, None, None))
    )
    when(mockObligationsConnector.get(any[String])(any())).thenReturn(Future.successful(obligation))
  }

  private def verifyResults(obligation: PPTObligations) = {
    val captor: ArgumentCaptor[Option[PPTObligations]] =
      ArgumentCaptor.forClass(classOf[Option[PPTObligations]])
    verify(page).apply(any(), any(), captor.capture(), any(), any(), any())(any(), any())

    captor.getValue.get mustBe obligation
  }

  private def createDefaultPPTObligation: PPTObligations =
    PPTObligations(None, None, 1, true, true)

}

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

import org.mockito.Mockito.when
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.mvc.{Headers, Results}
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.{
  Enrolment,
  IncorrectCredentialStrength,
  InternalError,
  MissingBearerToken
}
import uk.gov.hmrc.plasticpackagingtax.returns.base.PptTestData.{
  newEnrolment,
  newEnrolments,
  pptEnrolment
}
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.ControllerSpec
import uk.gov.hmrc.plasticpackagingtax.returns.base.{MetricsMocks, PptTestData}
import uk.gov.hmrc.plasticpackagingtax.returns.config.AppConfig
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.actions.AuthAction.{
  pptEnrolmentIdentifierName,
  pptEnrolmentKey
}
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.agents.{routes => agentRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.home.{routes => homeRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.models.request.AuthenticatedRequest

import scala.concurrent.Future

class AuthActionSpec extends ControllerSpec with MetricsMocks {

  private val appConfig = mock[AppConfig]

  private def createAuthAction(
    pptReferenceAllowedList: PptReferenceAllowedList = new PptReferenceAllowedList(Seq.empty)
  ): AuthAction =
    new AuthActionImpl(mockAuthConnector,
                       pptReferenceAllowedList,
                       appConfig,
                       metricsMock,
                       stubMessagesControllerComponents()
    )

  private val okResponseGenerator = (_: AuthenticatedRequest[_]) => Future(Results.Ok)

  "Auth Action" should {

    "redirect to not enrolled page when enrolment id is missing" in {
      val user = PptTestData.newUser("123", Some(pptEnrolment("")))
      authorizedUser(user)

      val result = createAuthAction().invokeBlock(authRequest(Headers(), user), okResponseGenerator)
      redirectLocation(result) mustBe Some(homeRoutes.UnauthorisedController.notEnrolled().url)
    }

    "redirect agents to client identification page if we can see client identifier on the agents session" in {
      val agent = PptTestData.newAgent("456")
      authorizedUser(agent)

      val result =
        createAuthAction().invokeBlock(authRequest(Headers(), agent), okResponseGenerator)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(agentRoutes.AgentsController.displayPage().url)
    }

    "process request when enrolment id is present on a normal user's auth response" in {
      val user = PptTestData.newUser("123", Some(pptEnrolment("555")))
      authorizedUser(user)

      await(
        createAuthAction().invokeBlock(authRequest(Headers(), user), okResponseGenerator)
      ) mustBe Results.Ok
    }

    "process request when an authorised client identifier is seen on an agents session" in {
      val agent = PptTestData.newAgent("456")

      val agentDelegatedAuthPredicate =
        Enrolment("HMRC-PPT-ORG").withIdentifier(pptEnrolmentIdentifierName,
                                                 "XMPPT0000000123"
        ).withDelegatedAuthRule("ppt-auth").and(expectedAcceptableCredentialsPredicate)
      authorizedUser(agent, requiredPredicate = agentDelegatedAuthPredicate)
      await(
        createAuthAction().invokeBlock(authRequest(Headers(), agent, Some("XMPPT0000000123")),
                                       okResponseGenerator
        )
      ) mustBe Results.Ok
    }

    "process request when enrolment id is present and multiple identifier exist for same key" in {
      val user = PptTestData.newUser(
        "123",
        Option(
          newEnrolments(newEnrolment(pptEnrolmentKey, pptEnrolmentIdentifierName, "555"),
                        newEnrolment(pptEnrolmentKey, "ABC-Name", "999")
          )
        )
      )
      authorizedUser(user)

      await(
        createAuthAction().invokeBlock(authRequest(Headers(), user), okResponseGenerator)
      ) mustBe Results.Ok
    }

    "redirect to not enrolled page when enrolment id is not present and multiple identifier exist for same key" in {
      val user = PptTestData.newUser(
        "123",
        Some(
          newEnrolments(newEnrolment(AuthAction.pptEnrolmentKey, "DEF-NAME", "555"),
                        newEnrolment(AuthAction.pptEnrolmentKey, "ABC-Name", "999")
          )
        )
      )
      authorizedUser(user)

      val result = createAuthAction().invokeBlock(authRequest(Headers(), user), okResponseGenerator)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(homeRoutes.UnauthorisedController.notEnrolled().url)
    }

    "redirect to not enrolled page when enrolment id is present but no ppt enrolment key found" in {
      val user = PptTestData.newUser("123",
                                     Some(
                                       newEnrolments(
                                         newEnrolment("SOME-OTHER-KEY",
                                                      AuthAction.pptEnrolmentIdentifierName,
                                                      "555"
                                         ),
                                         newEnrolment(AuthAction.pptEnrolmentKey, "ABC-Name", "999")
                                       )
                                     )
      )
      authorizedUser(user)

      val result = createAuthAction().invokeBlock(authRequest(Headers(), user), okResponseGenerator)

      redirectLocation(result) mustBe Some(homeRoutes.UnauthorisedController.notEnrolled().url)
    }

    "time calls to authorisation" in {
      val user = PptTestData.newUser("123", Some(pptEnrolment("555")))
      authorizedUser(user)

      await(createAuthAction().invokeBlock(authRequest(Headers(), user), okResponseGenerator))
      metricsMock.defaultRegistry.timer("ppt.returns.upstream.auth.timer").getCount should be > 0L
    }

    "process request when enrolment id is present and allowed" in {
      val enrolmentId = "555"
      val user        = PptTestData.newUser("123", Some(pptEnrolment(enrolmentId)))
      authorizedUser(user)

      await(
        createAuthAction(new PptReferenceAllowedList(Seq(enrolmentId))).invokeBlock(
          authRequest(Headers(), user),
          okResponseGenerator
        )
      ) mustBe Results.Ok
    }

    "redirect to home when enrolment id is present but not allowed" in {
      val user = PptTestData.newUser("123", Some(pptEnrolment("555")))
      authorizedUser(user)

      val result =
        createAuthAction(new PptReferenceAllowedList(Seq("someOtherEnrolmentId"))).invokeBlock(
          authRequest(Headers(), user),
          okResponseGenerator
        )

      redirectLocation(result) mustBe Some(homeRoutes.UnauthorisedController.unauthorised().url)
    }

    "redirect when user not logged in" in {

      when(appConfig.loginUrl).thenReturn("login-url")
      when(appConfig.loginContinueUrl).thenReturn("login-continue-url")

      whenAuthFailsWith(MissingBearerToken())

      val result =
        createAuthAction().invokeBlock(authRequest(Headers(), PptTestData.newUser()),
                                       okResponseGenerator
        )

      redirectLocation(result) mustBe Some("login-url?continue=login-continue-url")
    }

    "redirect the user to MFA Uplift page if the user has incorrect credential strength " in {
      when(appConfig.mfaUpliftUrl).thenReturn("mfa-uplift-url")
      when(appConfig.loginContinueUrl).thenReturn("login-continue-url")
      when(appConfig.serviceIdentifier).thenReturn("PPT")

      whenAuthFailsWith(IncorrectCredentialStrength())
      val result =
        createAuthAction().invokeBlock(authRequest(Headers(), PptTestData.newUser()),
                                       okResponseGenerator
        )

      redirectLocation(result) mustBe Some(
        "mfa-uplift-url?origin=PPT&continueUrl=login-continue-url"
      )
    }

    "redirect to unauthorised page when authorisation fails for a reason unrelated to insufficient enrolments" in {
      whenAuthFailsWith(InternalError("Some unexpected auth error"))

      val result =
        createAuthAction().invokeBlock(authRequest(Headers(), PptTestData.newUser()),
                                       okResponseGenerator
        )

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(homeRoutes.UnauthorisedController.unauthorised().url)
    }
  }
}

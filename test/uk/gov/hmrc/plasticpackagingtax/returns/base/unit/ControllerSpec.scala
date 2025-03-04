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

import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.JsValue
import play.api.mvc._
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.Helpers.contentAsString
import play.api.test.{DefaultAwaitTimeout, FakeRequest}
import play.twirl.api.Html
import uk.gov.hmrc.auth.core.{AffinityGroup, CredentialStrength}
import uk.gov.hmrc.plasticpackagingtax.returns.base.PptTestData.pptEnrolment
import uk.gov.hmrc.plasticpackagingtax.returns.base.{MetricsMocks, MockAuthAction, PptTestData}
import uk.gov.hmrc.plasticpackagingtax.returns.config.AppConfig
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.actions.{AuthAction, SaveAndContinue}
import uk.gov.hmrc.plasticpackagingtax.returns.models.SignedInUser
import uk.gov.hmrc.plasticpackagingtax.returns.models.request.AuthenticatedRequest

import java.lang.reflect.Field
import scala.concurrent.{ExecutionContext, Future}

trait ControllerSpec
    extends AnyWordSpecLike with MockitoSugar with Matchers with GuiceOneAppPerSuite
    with BeforeAndAfterEach with DefaultAwaitTimeout with MockJourneyAction with MockAuthAction
    with MockSubscriptionConnector with MockObligationsConnector with MetricsMocks {

  implicit val ec: ExecutionContext = ExecutionContext.global

  implicit val config: AppConfig = mock[AppConfig]

  protected val saveAndContinueFormAction: (String, String) = (SaveAndContinue.toString, "")

  def getRequest(session: (String, String) = "" -> ""): Request[AnyContentAsEmpty.type] =
    FakeRequest("GET", "").withSession(session)

  protected def viewOf(result: Future[Result]): Html = Html(contentAsString(result))

  def authRequest(
    headers: Headers = Headers(),
    user: SignedInUser = PptTestData.newUser("123", Some(pptEnrolment("333"))),
    pptClient: Option[String] = None
  ): AuthenticatedRequest[AnyContentAsEmpty.type] = {
    val request = pptClient.map { clientIdentifier =>
      FakeRequest().withHeaders(headers).withSession(("clientPPT", clientIdentifier))
    }.getOrElse {
      FakeRequest().withHeaders(headers)
    }

    new AuthenticatedRequest(
      request,
      user,
      user.enrolments.getEnrolment(AuthAction.pptEnrolmentKey).flatMap(
        e => e.getIdentifier(AuthAction.pptEnrolmentIdentifierName).map(i => i.value)
      )
    )
  }

  protected def postRequestEncoded(
    form: AnyRef,
    formAction: (String, String) = saveAndContinueFormAction
  ): Request[AnyContentAsFormUrlEncoded] = {
    val bodyForm: Seq[(String, String)] = getTuples(form) :+ formAction
    postRequest
      .withFormUrlEncodedBody(bodyForm: _*)
      .withCSRFToken
  }

  // This function exists because getTuples used in the above may not always encode values correctly
  protected def postRequestTuplesEncoded(
    formTuples: Seq[(String, String)],
    formAction: (String, String) = saveAndContinueFormAction
  ): Request[AnyContentAsFormUrlEncoded] =
    postRequest
      .withFormUrlEncodedBody(formTuples :+ formAction: _*)
      .withCSRFToken

  protected def postJsonRequestEncoded(
    body: (String, String)*
  ): Request[AnyContentAsFormUrlEncoded] =
    postRequest
      .withFormUrlEncodedBody(body: _*)
      .withCSRFToken

  protected def postRequest(body: JsValue): Request[AnyContentAsJson] =
    postRequest
      .withJsonBody(body)
      .withCSRFToken

  protected def postRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("POST", "")

  protected def getTuples(cc: AnyRef): Seq[(String, String)] =
    cc.getClass.getDeclaredFields.foldLeft(Map.empty[String, String]) { (a, f) =>
      f.setAccessible(true)
      a + (f.getName -> getValue(f, cc))
    }.toList

  protected val expectedAcceptableCredentialsPredicate =
    AffinityGroup.Agent.or(CredentialStrength(CredentialStrength.strong))

  private def getValue(field: Field, cc: AnyRef): String =
    field.get(cc) match {
      case o: Option[_] if o.isDefined => o.get.toString
      case c: String                   => c
      case _                           => ""
    }

}

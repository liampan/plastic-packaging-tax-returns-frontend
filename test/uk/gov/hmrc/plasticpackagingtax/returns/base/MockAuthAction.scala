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

package uk.gov.hmrc.plasticpackagingtax.returns.base

import org.joda.time.DateTimeZone.UTC
import org.joda.time.{DateTime, LocalDate}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers.stubMessagesControllerComponents
import uk.gov.hmrc.auth.core.AffinityGroup.Individual
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.plasticpackagingtax.returns.base.PptTestData.{newUser, pptEnrolment}
import uk.gov.hmrc.plasticpackagingtax.returns.config.AppConfig
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.actions.{
  AuthActionImpl,
  PptReferenceAllowedList
}
import uk.gov.hmrc.plasticpackagingtax.returns.models.SignedInUser

import scala.concurrent.Future

trait MockAuthAction extends MockitoSugar with MetricsMocks {

  val mockAuthConnector: AuthConnector = mock[AuthConnector]

  private val appConfig = mock[AppConfig]

  val mockAuthAction = new AuthActionImpl(mockAuthConnector,
                                          new PptReferenceAllowedList(Seq.empty),
                                          appConfig,
                                          metricsMock,
                                          stubMessagesControllerComponents()
  )

  val exampleUser             = newUser("external1", Some(pptEnrolment("123")))
  val nrsGroupIdentifierValue = Some("groupIdentifierValue")
  val nrsCredentialRole       = Some(User)
  val nrsMdtpInformation      = MdtpInformation("deviceId", "sessionId")
  val nrsItmpName             = ItmpName(Some("givenName"), Some("middleName"), Some("familyName"))

  val nrsItmpAddress =
    ItmpAddress(Some("line1"),
                Some("line2"),
                Some("line3"),
                Some("line4"),
                Some("line5"),
                Some("postCode"),
                Some("countryName"),
                Some("countryCode")
    )

  val nrsAffinityGroup            = Some(Individual)
  val nrsCredentialStrength       = Some("STRONG")
  val nrsDateOfBirth              = Some(LocalDate.now().minusYears(25))
  val currentLoginTime: DateTime  = new DateTime(1530442800000L, UTC)
  val previousLoginTime: DateTime = new DateTime(1530464400000L, UTC)
  val nrsLoginTimes               = LoginTimes(currentLoginTime, Some(previousLoginTime))

  // format: off
  def authorizedUser(user: SignedInUser = exampleUser, requiredPredicate: Predicate = Enrolment("HMRC-PPT-ORG").
    and(AffinityGroup.Agent.or(CredentialStrength(CredentialStrength.strong)))): Unit = {
    when(
      mockAuthConnector.authorise(
        ArgumentMatchers.eq(requiredPredicate),
        ArgumentMatchers.eq(
          credentials and name and email and externalId and internalId and affinityGroup and allEnrolments
            and agentCode and confidenceLevel and nino and saUtr and dateOfBirth and agentInformation and groupIdentifier and
            credentialRole and mdtpInformation and itmpName and itmpDateOfBirth and itmpAddress and credentialStrength and loginTimes
        )
      )(any(), any())
    ).thenReturn(
      Future.successful(
        new ~(
          new ~(
            new ~(
              new ~(
                new ~(
                  new ~(
                    new ~(
                      new ~(
                        new ~(
                          new ~(
                            new ~(
                              new ~(
                                new ~(
                                  new ~(
                                    new ~(
                                      new ~(
                                        new ~(
                                          new ~(
                                            new ~(
                                              new ~(
                                                user.identityData.credentials,
                                                user.identityData.name
                                              ),
                                              user.identityData.email
                                            ),
                                            user.identityData.externalId
                                          ),
                                          user.identityData.internalId
                                        ),
                                        user.identityData.affinityGroup
                                      ),
                                      user.enrolments
                                    ),
                                    user.identityData.agentCode
                                  ),
                                  user.identityData.confidenceLevel.get
                                ),
                                user.identityData.nino
                              ),
                              user.identityData.saUtr
                            ),
                            user.identityData.dateOfBirth
                          ),
                          user.identityData.agentInformation.get
                        ),
                        nrsGroupIdentifierValue
                      ),
                      nrsCredentialRole
                    ),
                    Some(nrsMdtpInformation)
                  ),
                  Some(nrsItmpName)
                ),
                nrsDateOfBirth
              ),
              Some(nrsItmpAddress)
            ),
            nrsCredentialStrength
          ),
          nrsLoginTimes
        )
      )
    )
  }
  // format: on

  def unAuthorizedUser(): Unit =
    when(
      mockAuthConnector.authorise(any(),
                                  ArgumentMatchers.eq(
                                    credentials and name and email and externalId and internalId and affinityGroup and allEnrolments
                                      and agentCode and confidenceLevel and nino and saUtr and dateOfBirth and agentInformation and groupIdentifier and
                                      credentialRole and mdtpInformation and itmpName and itmpDateOfBirth and itmpAddress and credentialStrength and loginTimes
                                  )
      )(any(), any())
    ).thenReturn(Future.failed(new RuntimeException))

  def whenAuthFailsWith(exc: AuthorisationException): Unit =
    when(mockAuthConnector.authorise(any(), any())(any(), any()))
      .thenReturn(Future.failed(exc))

}

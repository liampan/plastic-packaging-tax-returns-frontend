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

package uk.gov.hmrc.plasticpackagingtax.returns.views.model

import org.scalatest.EitherValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.MessagesSupport

class SignOutReasonSpec extends AnyWordSpec with MessagesSupport with Matchers with EitherValues {

  "Sign out request params binder" should {

    "bind request params" when {

      "valid" in {

        val result =
          SignOutReason.binder.bind("signOutReason",
                                    Map("signOutReason" -> Seq(SignOutReason.UserAction.toString))
          )

        result.get.right.get mustBe SignOutReason.UserAction
      }
    }

    "bind session timeout request params" when {

      "not valid" in {

        val result =
          SignOutReason.binder.bind("signOutReason",
                                    Map("someRubbishKey" -> Seq(SignOutReason.UserAction.toString))
          )

        result.get.right.get mustBe SignOutReason.SessionTimeout
      }
    }
  }

  "unbind request params" in {

    val result =
      SignOutReason.binder.unbind("signOutReason", SignOutReason.UserAction)

    result mustBe s"signOutReason=${SignOutReason.UserAction}"

  }
}

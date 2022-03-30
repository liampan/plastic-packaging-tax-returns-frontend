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

package forms

import forms.behaviours.DecimalFieldBehaviours
import org.scalacheck.Gen
import play.api.data.FormError

import scala.math.BigDecimal.RoundingMode

class ConvertedPackagingCreditFormProviderSpec extends DecimalFieldBehaviours {

  val packagingCredit = 1.25
  val form            = new ConvertedPackagingCreditFormProvider()()

  ".value" - {

    ".value" - {

      val fieldName = "value"

      val minimum = BigDecimal(0.01)
      val maximum = BigDecimal(99999999.99)

      val validDataGenerator =
        Gen.choose[BigDecimal](minimum, maximum)
          .map(_.setScale(2, RoundingMode.HALF_UP))
          .map(_.toString)

      behave like fieldThatBindsValidData(form, fieldName, validDataGenerator)

      behave like decimalField(form,
                               fieldName,
                               nonNumericError =
                                 FormError(fieldName,
                                           "convertedPackagingCredit.error.nonNumeric",
                                           Seq(packagingCredit)
                                 ),
                               invalidNumericError =
                                 FormError(fieldName,
                                           "convertedPackagingCredit.error.wholeNumber",
                                           Seq(packagingCredit)
                                 )
      )

      behave like decimalFieldWithRange(form,
                                        fieldName,
                                        minimum = minimum,
                                        maximum = maximum,
                                        expectedError =
                                          FormError(fieldName,
                                                    "convertedPackagingCredit.error.outOfRange",
                                                    Seq(minimum, maximum)
                                          )
      )

      behave like mandatoryField(form,
                                 fieldName,
                                 requiredError =
                                   FormError(fieldName,
                                             "convertedPackagingCredit.error.required",
                                             Seq(packagingCredit)
                                   )
      )
    }
  }
}

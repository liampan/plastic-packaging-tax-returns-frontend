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

package controllers.helpers

import models.UserAnswers
import models.returns._
import pages._

import java.time.LocalDate

class TaxReturnHelper {

  // TODO - where do we get this obligation from? A GET on the return?
  val defaultObligation = TaxReturnObligation(fromDate = LocalDate.parse("2022-04-01"),
                                              toDate = LocalDate.parse("2022-06-30"),
                                              dueDate = LocalDate.parse("2022-09-30"),
                                              periodKey = "22AC"
  )

  def getTaxReturn(pptReference: String, userAnswers: UserAnswers): TaxReturn =
    TaxReturn(id = pptReference,
              returnType = Some(ReturnType.AMEND),
              obligation = Some(defaultObligation),
              manufacturedPlasticWeight =
                userAnswers.get(AmendManufacturedPlasticPackagingPage).map(
                  value => ManufacturedPlasticWeight(value)
                ),
              importedPlasticWeight =
                userAnswers.get(AmendImportedPlasticPackagingPage).map(
                  value => ImportedPlasticWeight(value)
                ),
              humanMedicinesPlasticWeight =
                userAnswers.get(AmendHumanMedicinePlasticPackagingPage).map(
                  value => HumanMedicinesPlasticWeight(value)
                ),
              exportedPlasticWeight =
                userAnswers.get(AmendDirectExportPlasticPackagingPage).map(
                  value => ExportedPlasticWeight(value)
                ),
              recycledPlasticWeight = userAnswers.get(AmendRecycledPlasticPackagingPage).map(
                value => RecycledPlasticWeight(value)
              )
    )

}

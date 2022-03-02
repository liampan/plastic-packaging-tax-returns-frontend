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

package uk.gov.hmrc.plasticpackagingtax.returns.models.domain

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.plasticpackagingtax.returns.utils.TaxLiabilityFactory

case class TaxReturn(
  id: String,
  manufacturedPlastic: Option[Boolean] = None,
  manufacturedPlasticWeight: Option[ManufacturedPlasticWeight] = None,
  importedPlasticWeight: Option[ImportedPlasticWeight] = None,
  humanMedicinesPlasticWeight: Option[HumanMedicinesPlasticWeight] = None,
  exportedPlasticWeight: Option[ExportedPlasticWeight] = None,
  convertedPackagingCredit: Option[ConvertedPackagingCredit] = None,
  recycledPlasticWeight: Option[RecycledPlasticWeight] = None,
  metaData: MetaData = MetaData()
) {

  import TaxReturn.LongOption

  lazy val taxLiability = TaxLiabilityFactory.create(
    totalManufacturedKg = manufacturedPlasticWeight.map(_.totalKg).getOrZero,
    totalImportedKg = importedPlasticWeight.map(_.totalKg).getOrZero,
    totalHumanMedicinesKg = humanMedicinesPlasticWeight.map(_.totalKg).getOrZero,
    totalDirectExportsKg = exportedPlasticWeight.map(_.totalKg).getOrZero,
    totalConversionCreditPence = convertedPackagingCredit.map(_.totalInPence).getOrZero,
    totalRecycledKg = recycledPlasticWeight.map(_.totalKg).getOrZero
  )

  def toTaxReturn: TaxReturn =
    TaxReturn(id = this.id,
              manufacturedPlastic = this.manufacturedPlastic,
              manufacturedPlasticWeight = this.manufacturedPlasticWeight,
              importedPlasticWeight = this.importedPlasticWeight,
              humanMedicinesPlasticWeight = this.humanMedicinesPlasticWeight,
              exportedPlasticWeight = this.exportedPlasticWeight,
              convertedPackagingCredit = this.convertedPackagingCredit,
              recycledPlasticWeight = this.recycledPlasticWeight,
              metaData = this.metaData
    )

  def isReturnSubmitReady: Boolean =
    manufacturedPlasticWeight.isDefined && importedPlasticWeight.isDefined && humanMedicinesPlasticWeight.isDefined && exportedPlasticWeight.isDefined && convertedPackagingCredit.isDefined && recycledPlasticWeight.isDefined

}

object TaxReturn {
  implicit val format: OFormat[TaxReturn] = Json.format[TaxReturn]

  implicit class LongOption(option: Option[Long]) {
    def getOrZero: Long = option.getOrElse(0)
  }

}

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

package models.returns

import play.api.libs.json.{Json, OFormat}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}

import java.time.LocalDate

case class IdDetails(pptReferenceNumber: String, submissionId: String)

object IdDetails {
  implicit val format: OFormat[IdDetails] = Json.format[IdDetails]
}

case class ReturnDisplayChargeDetails(
  periodKey: String,
  chargeReference: Option[String],
  periodFrom: String,
  periodTo: String,
  receiptDate: String,
  returnType: String
)

object ReturnDisplayChargeDetails {
  implicit val format: OFormat[ReturnDisplayChargeDetails] = Json.format[ReturnDisplayChargeDetails]
}

case class ReturnDisplayDetails(
  manufacturedWeight: BigDecimal,
  importedWeight: BigDecimal,
  totalNotLiable: BigDecimal,
  humanMedicines: BigDecimal,
  directExports: BigDecimal,
  recycledPlastic: BigDecimal,
  creditForPeriod: BigDecimal,
  debitForPeriod: BigDecimal,
  totalWeight: BigDecimal,
  taxDue: BigDecimal
){
  def liableWeight: BigDecimal = manufacturedWeight + importedWeight
}

object ReturnDisplayDetails {
  implicit val format: OFormat[ReturnDisplayDetails] = Json.format[ReturnDisplayDetails]
}

case class ReturnDisplayApi(
  processingDate: String,
  idDetails: IdDetails,
  chargeDetails: Option[ReturnDisplayChargeDetails],
  returnDetails: ReturnDisplayDetails
){

  def chargeReferenceAsString: String =
    chargeDetails.flatMap(_.chargeReference).getOrElse("n/a")

}

object ReturnDisplayApi {
  implicit val format: OFormat[ReturnDisplayApi] = Json.format[ReturnDisplayApi]
}

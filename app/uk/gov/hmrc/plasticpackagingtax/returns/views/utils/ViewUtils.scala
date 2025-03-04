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

package uk.gov.hmrc.plasticpackagingtax.returns.views.utils

import play.api.i18n.Messages
import uk.gov.hmrc.plasticpackagingtax.returns.models.obligations.Obligation

import java.time.LocalDate
import scala.math.BigDecimal.RoundingMode

object ViewUtils {

  def displayMonetaryValue(v: BigDecimal): String = s"£${v.setScale(2, RoundingMode.HALF_EVEN)}"

  def displayReturnQuarter(obligation: Obligation)(implicit messages: Messages): String =
    messages("return.quarter",
             getMonthName(obligation.fromDate.getMonthValue),
             getMonthName(obligation.toDate.getMonthValue),
             obligation.toDate.getYear.toString
    )

  def displayLocalDate(date: LocalDate)(implicit messages: Messages): String =
    s"${date.getDayOfMonth} ${getMonthName(date.getMonthValue)} ${date.getYear}"

  private def getMonthName(monthNumber: Int)(implicit messages: Messages): String =
    messages(s"month.$monthNumber")

}

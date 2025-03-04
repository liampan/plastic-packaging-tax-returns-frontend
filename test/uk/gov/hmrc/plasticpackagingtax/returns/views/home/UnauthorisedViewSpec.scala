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

package uk.gov.hmrc.plasticpackagingtax.returns.views.home

import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.UnitViewSpec
import uk.gov.hmrc.plasticpackagingtax.returns.config.AppConfig
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.home.unauthorised
import uk.gov.hmrc.plasticpackagingtax.returns.views.tags.ViewTest

@ViewTest
class UnauthorisedViewSpec extends UnitViewSpec with Matchers {

  private val page      = instanceOf[unauthorised]
  private val appConfig = instanceOf[AppConfig]

  private def createView(): Document =
    page()(request, messages)

  override def exerciseGeneratedRenderingMethods(): Unit = {
    page.f()(request, messages)
    page.render(request, messages)
  }

  "Unauthorised Page view" should {

    val view = createView()

    "display page header" in {
      view.getElementsByTag("h1").first() must containMessage("unauthorised.heading")
    }

    "display register for ppt link" in {
      val link = view.getElementById("register_for_ppt_link")

      link must containMessage("unauthorised.paragraph.1.link")
      link must haveHref(appConfig.pptRegistrationUrl)
      link.attributes().hasKey("target") mustBe false
    }

    "display ppt guidance link" in {
      val link = view.getElementById("find_out_about_ppt_link")

      link must containMessage("unauthorised.paragraph.2.link")
      link must haveHref(
        "https://www.gov.uk/guidance/check-if-you-need-to-register-for-plastic-packaging-tax"
      )
      link.attributes().hasKey("target") mustBe false
    }
  }
}

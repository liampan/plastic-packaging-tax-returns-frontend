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

package uk.gov.hmrc.plasticpackagingtax.returns.controllers.agents

import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.actions.AuthAgentAction
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.home.{routes => homeRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.forms.agents.ClientIdentifier
import uk.gov.hmrc.plasticpackagingtax.returns.forms.agents.ClientIdentifier.identifier
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.agents.agents_page
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AgentsController @Inject() (
  authenticate: AuthAgentAction,
  mcc: MessagesControllerComponents,
  page: agents_page
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with SelectedClientIdentifier {

  val displayPage: Action[AnyContent] =
    authenticate { implicit request =>
      val currentlySelectedClientIdentifier = getSelectedClientIdentifierFrom(request)

      // Look for a flash signal if the client identifier on the session has failed auth
      request.flash.get("clientPPTFailed") match {
        case Some(_) =>
          val form = ClientIdentifier.form().fill(
            ClientIdentifier(currentlySelectedClientIdentifier.getOrElse(""))
          ).withError(identifier, "agents.client.identifier.auth.error")
          Forbidden(page(form))
        case _ =>
          val form = ClientIdentifier.form().fill(
            ClientIdentifier(currentlySelectedClientIdentifier.getOrElse(""))
          )
          Ok(page(form))
      }
    }

  val submit: Action[AnyContent] =
    authenticate { implicit request =>
      ClientIdentifier.form()
        .bindFromRequest()
        .fold((formWithErrors: Form[ClientIdentifier]) => BadRequest(page(formWithErrors)),
              clientIdentifier =>
                // Set this on the session and then redirect to account page to attempt to auth with it
                appendSelectedClientIdentifierToResult(
                  clientIdentifier,
                  Redirect(homeRoutes.HomeController.displayPage())
                )
        )
    }

}

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

package uk.gov.hmrc.plasticpackagingtax.returns.controllers.home

import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.plasticpackagingtax.returns.config.AppConfig
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.home.session_timed_out
import uk.gov.hmrc.plasticpackagingtax.returns.views.model.SignOutReason
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.Future

class SignOutController @Inject() (
  appConfig: AppConfig,
  sessionTimedOut: session_timed_out,
  mcc: MessagesControllerComponents
) extends FrontendController(mcc) with I18nSupport {

  def signOut(signOutReason: SignOutReason): Action[AnyContent] =
    Action.async { implicit request =>
      signOutReason match {
        case SignOutReason.SessionTimeout =>
          Future.successful(
            Redirect(routes.SignOutController.sessionTimeoutSignedOut()).withNewSession
          )
        case SignOutReason.UserAction =>
          Future.successful(Redirect(appConfig.exitSurveyUrl).withNewSession)
      }
    }

  def sessionTimeoutSignedOut: Action[AnyContent] =
    Action.async { implicit request =>
      Future.successful(Ok(sessionTimedOut()))
    }

}

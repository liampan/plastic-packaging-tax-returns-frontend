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

package controllers.actions

import javax.inject.Inject
import models.requests.{IdentifiedRequest, OptionalDataRequest}
import play.api.mvc.ActionTransformer
import repositories.SessionRepository

import scala.concurrent.{ExecutionContext, Future}

class DataRetrievalActionImpl @Inject() (val sessionRepository: SessionRepository)(implicit
  val executionContext: ExecutionContext
) extends DataRetrievalAction {

  override protected def transform[A](
    request: IdentifiedRequest[A]
  ): Future[OptionalDataRequest[A]] =
    sessionRepository.get(request.user.identityData.internalId.getOrElse("")).map {
      case None =>
        OptionalDataRequest(request.request,
                            request.user.identityData.internalId.getOrElse(""),
                            None
        )
      case Some(userAnswers) =>
        OptionalDataRequest(request.request,
                            request.user.identityData.internalId.getOrElse(""),
                            Some(userAnswers)
        )
    }

}

trait DataRetrievalAction extends ActionTransformer[IdentifiedRequest, OptionalDataRequest]

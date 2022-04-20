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

import connectors.CacheConnector

import javax.inject.Inject
import models.requests.{IdentifiedRequest, OptionalDataRequest}
import play.api.mvc.ActionTransformer
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class DataRetrievalActionImpl @Inject() (
                                          val sessionRepository: SessionRepository,
                                          val cacheConnector: CacheConnector
                                        )(implicit
  val executionContext: ExecutionContext
) extends DataRetrievalAction {

  override protected def transform[A](
                                       request: IdentifiedRequest[A]
                                     ): Future[OptionalDataRequest[A]] =
    sessionRepository.get(request.user.identityData.internalId).map {
      OptionalDataRequest(request, request.user.identityData.internalId, _)
    }

  protected def transform[A](
    request: IdentifiedRequest[A]
  )(implicit hc: HeaderCarrier): Future[OptionalDataRequest[A]] =
    cacheConnector.get(request.user.identityData.internalId).map {
      OptionalDataRequest(request, request.user.identityData.internalId, _)
    }

}

trait DataRetrievalAction extends ActionTransformer[IdentifiedRequest, OptionalDataRequest]

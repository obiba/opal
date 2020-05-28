/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.rest.client.event;

import org.obiba.opal.web.model.client.ws.ConstraintViolationErrorDto;

import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.dispatch.annotation.Optional;
import com.gwtplatform.dispatch.annotation.Order;

/**
 * An event fired when an exception is raised during the processing of a request built by the {@link
 * ResourceRequestBuilder}
 */
@GenEvent
public class RequestError {

  @Order(1)
  @Optional
  Throwable exception;

  @Order(2)
  @Optional
  String message;

  @Order(3)
  @Optional
  Iterable<ConstraintViolationErrorDto> violations;

}

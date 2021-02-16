/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.r;

import org.obiba.opal.spi.r.REvaluationRuntimeException;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Component
@Provider
public class REvaluationRuntimeExceptionMapper implements ExceptionMapper<REvaluationRuntimeException> {

  @Override
  public Response toResponse(REvaluationRuntimeException exception) {
    return Response
        .status(Status.BAD_REQUEST)
        .type("application/x-protobuf+json")
        .entity(Dtos.getErrorMessage(Status.BAD_REQUEST, exception))
        .build();
  }

}

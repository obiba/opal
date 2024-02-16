/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.r;

import org.obiba.opal.spi.r.RServerException;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Component
@Provider
public class RServerExceptionMapper implements ExceptionMapper<RServerException> {

  @Override
  public Response toResponse(RServerException exception) {
    if (exception.isClientError())
      return Response
          .status(Status.BAD_REQUEST)
          .type("application/x-protobuf+json")
          .entity(Dtos.getErrorMessage(Status.BAD_REQUEST, exception))
          .build();
    else
      return Response
          .status(Status.INTERNAL_SERVER_ERROR)
          .type("application/x-protobuf+json")
          .entity(Dtos.getErrorMessage(Status.INTERNAL_SERVER_ERROR, exception))
          .build();
  }

}

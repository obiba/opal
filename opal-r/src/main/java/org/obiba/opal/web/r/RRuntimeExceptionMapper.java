/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.r;

import org.obiba.opal.spi.r.RRuntimeException;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Component
@Provider
public class RRuntimeExceptionMapper implements ExceptionMapper<RRuntimeException> {

  @Override
  public Response toResponse(RRuntimeException exception) {
    return Response
      .status(Status.INTERNAL_SERVER_ERROR)
      .type("application/x-protobuf+json")
      .entity(Dtos.getErrorMessage(Status.INTERNAL_SERVER_ERROR, exception))
      .build();
  }

}

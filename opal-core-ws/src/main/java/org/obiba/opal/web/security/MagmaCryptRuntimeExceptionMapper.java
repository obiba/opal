/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.security;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.obiba.magma.crypt.MagmaCryptRuntimeException;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.springframework.stereotype.Component;

@Provider
@Component
public class MagmaCryptRuntimeExceptionMapper implements ExceptionMapper<MagmaCryptRuntimeException> {

  @Override
  public Response toResponse(MagmaCryptRuntimeException exception) {
    return Response.status(Response.Status.BAD_REQUEST).type("application/x-protobuf+json")
        .entity(ClientErrorDtos.getErrorMessage(Response.Status.BAD_REQUEST, "InvalidCertificate", exception)).build();
  }

}

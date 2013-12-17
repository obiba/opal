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

import java.security.InvalidParameterException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.obiba.opal.web.magma.ClientErrorDtos;
import org.springframework.stereotype.Component;


@Provider
@Component
public class MagmaCryptInvalidArgumentExceptionMapper implements ExceptionMapper<InvalidParameterException> {

  @Override
  public Response toResponse(InvalidParameterException exception) {
    return Response.status(Response.Status.BAD_REQUEST)
        .entity(ClientErrorDtos.getErrorMessage(Response.Status.BAD_REQUEST, "InvalidKeypair", exception)).build();
  }
}
